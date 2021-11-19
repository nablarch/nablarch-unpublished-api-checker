package nablarch.test.tool.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 非公開APIの使用を検出するfindbugsカスタムルールのdetector。
 * 
 * @author 香川朋和
 */
public class UsageOfUnpublishedMethodDetector extends BytecodeScanningDetector {

    /** バグコード。 */
    static final String BUG_CODE = "UPU_UNPUBLISHED_API_USAGE";

    /** FindBugsバグレポーター。 */
    private final BugReporter bugReporter;

    /**
     * コンストラクタ。
     * 
     * @param bugReporter バグリポート
     */
    public UsageOfUnpublishedMethodDetector(final BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    /** ExceptionTable内容。 */
    private CodeException[] codeExceptions;

    /** コンスタントプール内容。 */
    private Constant[] constants;

    /** finally節情報一覧。 */
    private List<FinallyClause> finallyClauses;
    
    /** finally節インデックス。 */ 
    private int finallyIndex;

    /** JSRモードでコンパイルされているか否かを表す。 */
    private boolean jsrMode = false;

    /**
     * メソッド定義毎に行う処理。
     * 
     * @param code メソッド定義情報
     */
    public void visitCode(final Code code) {

        checkUnpublishedExceptionAtThrows();

        jsrMode = false;
        constants = code.getConstantPool().getConstantPool();
        finallyIndex = 0;
        codeExceptions = code.getExceptionTable();
        loadFinallyClauses();

        super.visitCode(code);
    }

    /**
     * throws節に非公開例外を使用しているか否かをチェックする。
     */
    private void checkUnpublishedExceptionAtThrows() {

        String methodSig = getMethod().toString();
        int index = methodSig.lastIndexOf("throws");
        if (index < 0) {
            return;
        }

        String[] throwsExceptions = methodSig.substring(index + 7).split(",");
        for (String exName : throwsExceptions) {
            exName = exName.trim();
            if (!PublishedApisInfo.isPermitted(exName)) {
                doBugReport(exName);
            }
        }
    }

    /**
     * ExceptionTableから取得した、finally節情報リスト保存する。
     */
    private void loadFinallyClauses() {

        finallyClauses = new ArrayList<FinallyClause>();
        for (CodeException ex : codeExceptions) {
            // catchType が0の場合は finally句
            if (ex.getCatchType() == 0) {
                finallyClauses.add(new FinallyClause(ex.getHandlerPC()));
            }
        }
    }

    /**
     * オペコード読み込み毎に行う処理。
     * 
     * @param opecode オペコード
     */
    public void sawOpcode(final int opecode) {
        checkUnpublishedApiMethodCall(opecode);
        checkUnpublishedApiException(opecode);
    }

    /**
     * catch指定された例外が公開されているか否かをチェックする。
     * 
     * @param opecode オペコード
     */
    private void checkUnpublishedApiException(final int opecode) {

        // メソッド終了時にチェックを行う
        if (getPC() == getMaxPC()) {

            for (CodeException codeException : codeExceptions) {

                if (codeException.getCatchType() == 0) {
                    continue;
                }

                int nameIndex = getCatchTypeIndex(codeException.getCatchType());
                String exName = formatClassName(constants[nameIndex].toString()).replace('/', '.');
                if (!PublishedApisInfo.isPermitted(exName)) {
                    doBugReport(exName);
                }
            }
        }

        // finally句外はリターン
        if (!isInFinally(getPC())) {
            return;
        }

        // In Line finallyではない場合、リターン
        if (JSR == opecode) {
            jsrMode = true;
            return;
        }

        FinallyClause fc = finallyClauses.get(finallyIndex);
        if (ATHROW == opecode && !jsrMode) {
            fc.setCheck(true);
            return;
        }

        if (RET == opecode && jsrMode) {
            fc.setCheck(true);
            jsrMode = false;
        }
    }

    /**
     * ConstantPoolから取得したクラス情報をフルパスのクラス名に修正したストリングを返す。
     * 
     * @param exName 呼び出しAPIクラス名
     * @return ConstantPoolから取得したクラス情報をフルパスのクラス名に修正した文字列
     */
    private static String formatClassName(String exName) {

        int beginIndex = exName.indexOf("\"") + 1;
        int endIndex = exName.lastIndexOf("\"");
        return exName.substring(beginIndex, endIndex).trim();
    }

    /**
     * 非公開API使用の情報を保存する。
     * 
     * @param usedUnpublishedApi 使用されている非公開API名
     */
    private void doBugReport(String usedUnpublishedApi) {
        bugReporter.reportBug(new BugInstance(this, BUG_CODE, NORMAL_PRIORITY)
                                    .addString(usedUnpublishedApi)
                                    .addMethod(this)
                                    .addClassAndMethod(this)
                                    .addSourceLine(this));
    }

    /**
     * finally句か否かの判定をする。
     * 
     * @param currentPC 該当処理に対応するプログラムカウンタ
     * @return finally句内の場合、{@code true}
     */
    private boolean isInFinally(int currentPC) {

        FinallyClause fClause = null;
        for (int i = 0; i < finallyClauses.size(); i++) {
            fClause = finallyClauses.get(i);
            if (currentPC > fClause.getHandlerPC() && !fClause.isChecked()) {
                finallyIndex = i;
                return true;
            }
        }
        return false;
    }

    /**
     * 指定したcatchTypeに該当する例外クラスの情報が書かれている、ConstantPool[]のインデックスを返す。
     * 
     * @param catchType キャッチタイプ(ConstantPoolにおけるインデックス)
     * @return 指定したcatchTypeに該当する例外クラスの情報が書かれている、ConstantPool[]のインデックス
     */
    private int getCatchTypeIndex(final int catchType) {

        int index = -1;

        String nameIndex = constants[catchType].toString();
        int beginIndex = nameIndex.indexOf("=");
        int endIndex = nameIndex.lastIndexOf(")");
        index = Integer.parseInt((nameIndex.substring(beginIndex + 1, endIndex)).trim());

        return index;
    }

    /**
     * finally句チェック用クラス。
     */
    private static class FinallyClause {

        /** 例外ハンドラの先頭を示すpcカウンタ。 */
        private int handlerPC;
        
        /** チェックを行ったfinally節か否かを表す。 */
        private boolean check;

        /**
         * コンストラクタ。
         * 
         * @param handlerPC 例外ハンドラの先頭を示すpcカウンタ
         */
        public FinallyClause(int handlerPC) {
            this.handlerPC = handlerPC;
            this.check = false;
        }

        /**
         * ハンドラpcカウンタを取得する。
         * 
         * @return ハンドラpcカウンタ
         */
        public int getHandlerPC() {
            return this.handlerPC;
        }

        /**
         * チェックを行ったか否かを設定する。
         * 
         * @param check チェックを行ったか否かを示すboolean値
         */
        public void setCheck(final boolean check) {
            this.check = check;
        }

        /**
         * チェックを行ったfinally節か否かを返す。
         * 
         * @return チェックを行ったfinally説である場合、{@code true}
         */
        public boolean isChecked() {
            return check;
        }
    }

    /**
     * 非公開APIのメソッドコールを検出し、報告する。
     * 
     * @param opecode オペコード
     */
    private void checkUnpublishedApiMethodCall(final int opecode) {
        // メソッドコールでなければリターン
        if (!isInvocation(opecode)) {
            return;
        }

        String calleeClassName = getDottedClassConstantOperand();
        String calleeMethodName = getNameConstantOperand();
        String calleeMethodSig = getSigConstantOperand();

        // 非公開メソッドであればバグを報告
        if (!PublishedApisInfo.isPermitted(calleeClassName, calleeMethodName, calleeMethodSig)) {
            doBugReport(PublishedApisInfo.getCalleeApi(calleeClassName, calleeMethodName, calleeMethodSig));
        }
    }

    /**
     * 指定されたオペコード値がメソッドコール命令であれば{@code true}を返す。
     * 
     * @param opecode オペコード
     * @return 指定されたオペコード値がメソッドコール命令であれば{@code true}
     */
    private static boolean isInvocation(final int opecode) {
        return opecode == Constants.INVOKEVIRTUAL
                        || opecode == Constants.INVOKEINTERFACE
                        || opecode == Constants.INVOKESTATIC
                        || opecode == Constants.INVOKESPECIAL;
    }
}
