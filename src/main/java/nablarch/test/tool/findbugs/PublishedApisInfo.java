package nablarch.test.tool.findbugs;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 公開APIの情報を保持する。
 * 
 * @author 香川朋和
 */
public final class PublishedApisInfo {

    /** パッケージまたはクラス指定の公開情報。 */
    private static Set<String> publishedPackageOrClassSet;

    /** メソッドコンストラクタ指定の公開情報。 */
    private static Set<String> publishedMethodAndConstructorSet;

    static {
        readConfigFiles();
    }

    /**
     * コンストラクタ。
     */
    private PublishedApisInfo() {
    }

    /**
     * 設定ファイルが格納されているディレクトリからテキストファイルだけを設定ファイルとして読み込む。
     */
    static void readConfigFiles() {

        publishedMethodAndConstructorSet = new HashSet<String>();
        publishedPackageOrClassSet = new HashSet<String>();
        String configDirPath = System.getProperty("nablarch-findbugs-config");
        
        File configDir = new File(configDirPath);
        if (!configDir.exists() || !configDir.isDirectory()) {
            throw new RuntimeException("Config file directory doesn't exist.Path=[" + configDirPath + "]");
        }

        File[] configFiles = configDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".config");
            }
        });

        for (File configFile : configFiles) {
            readConfigFile(configFile);
        }
    }

    /**
     * 各設定ファイルを読み込む。
     * 
     * @param configFile 設定ファイル
     */
    private static void readConfigFile(File configFile) {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                // 「(」が存在すれば、メソッド指定公開
                if (line.contains("(")) {
                    publishedMethodAndConstructorSet.add(replaceInnerClassConstructor(line.replaceAll("\\s+", "").replaceAll("\\.{3}", "[]")));
                } else {
                    publishedPackageOrClassSet.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "Couldn't find config file. Path=[" + configFile + "]", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Couldn't read config file. Path=[" + configFile + "]", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed in closing config file. Path=[" + configFile + "]", e);
                }
            }
        }
    }

    /**
     * Innerクラスのコンストラクタ呼出を置き換える。
     *
     * Innerクラスの場合、許可ファイルに{@code xx.xxx.xxxx.Hoge.InnerHoge.Hoge.InnerHoge()}のように
     * コンストラクタ名が「内部クラスが定義されたクラス名 + "." + コンストラクタ」となっている。
     * これを{@code xx.xxx.xxxx.Hoge.InnerHoge.InnerHoge()}のように、「内部クラス名 + "." + コンストラクタ」に置き換える。
     *
     * @param methodCall 許可メソッドの呼出を表す文字列。
     * @return 置き換え後のコンストラクタ呼出定義
     */
    private static String replaceInnerClassConstructor(String methodCall) {
        int pos = methodCall.indexOf('(');

        String signature = methodCall.substring(pos);
        // 「.」毎に区切る
        List<String> items = Arrays.asList(methodCall.substring(0, pos).split("\\."));
        // メソッド名が複数あった場合、コンストラクタ呼出。
        String methodName = items.get(items.size() - 1);
        int methodNamePos = items.indexOf(methodName);
        if (methodNamePos == (items.size() - 1)) {
            // 内部クラスのコンストラクタ呼出じゃないのでそのまま返却
            return methodCall;
        }

        // Innerクラスのコンストラクタ呼出形式を構築する。
        StringBuilder sb = new StringBuilder();
        for (String item : items.subList(0, methodNamePos)) {
            sb.append(item);
            sb.append('.');
        }
        sb.append(methodName);
        sb.append(".");
        sb.append(methodName);
        sb.append(signature);
        return sb.toString();
    }

    /**
     * 呼び出されたメソッド・コンストラクタが公開されているか否かをチェックする。<br/>
     * 
     * @param calleeClassName 呼び出されたAPIのクラス名
     * @param calleeMethodName 呼び出されたAPIのメソッド名
     * @param calleeMethodSig 呼び出されたAPIメソッドシグネチャ
     * @return 指定したメソッドが公開されている場合、{@code true}
     */
    static boolean isPermitted(String calleeClassName, String calleeMethodName, String calleeMethodSig) {

        JavaClass calleeJavaClass;
        try {
            // 配列に対する呼出の場合
            if (calleeClassName.startsWith("[")) {
                calleeClassName = "java.lang.Object";
            }

            calleeJavaClass = Repository.lookupClass(calleeClassName);

            return isPermittedForClassOrInterface(calleeJavaClass, calleeMethodName, calleeMethodSig);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find JavaClass of itself or super class. ClassName=[" + calleeClassName + "]", e);
        }
    }

    /**
     * 指定したクラスが公開されているか否かをチェックする。
     * 
     * @param calleeClassName チェック対象クラス名
     * @return 指定したクラスが公開されている場合、{@code true}
     */
    static boolean isPermitted(String calleeClassName) {
        return checkForClassOrPackage(calleeClassName.replace('$', '.'));
    }

    /**
     * 呼び出されたメソッド・コンストラクタが公開されているか否かをチェックする。
     * 
     * @param calleeJavaClass 呼び出されたAPIのクラス情報
     * @param calleeMethodName 呼び出されたAPIのメソッド名
     * @param calleeMethodSig 呼び出されたAPIのシグネチャ
     * @return 呼び出されたメソッド・コンストラクタが公開されている場合、{@code true}
     * @throws ClassNotFoundException 親クラス情報を取得できない場合に発生する。
     */
    private static boolean isPermittedForClassOrInterface(JavaClass calleeJavaClass, String calleeMethodName, String calleeMethodSig)
            throws ClassNotFoundException {

        // ")"以後には戻り値の型が記述されているが、以後考慮しないため、切り捨てる。
        calleeMethodSig = calleeMethodSig.substring(0, calleeMethodSig.indexOf(")") + 1);
        Method method = getMethodOf(calleeJavaClass, calleeMethodName, calleeMethodSig);
        if (method != null) {
            // privateメソッドチェックしない
            if (method.isPrivate()) {
                return true;
            }
            // 自クラスがチェック対象のメソッドを定義している場合は、そのAPIが公開されているかチェックする。
            return checkPublicityForTheClass(calleeJavaClass, calleeMethodName, calleeMethodSig);
        } else {
            return checkSuperClassOrInterface(calleeJavaClass, calleeMethodName, calleeMethodSig);
        }
    }

    /**
     * calleeJavaClassがcalleeMethodNameAndSigで表されるメソッドを有する場合、そのメソッド情報を保持する{@link Method}を返却する。<br/>
     * 該当するメソッドがcalleeJavaClassになければ、{@code null}を返却する。
     *
     * @param calleeJavaClass チェック対象のクラス情報を保持する{@link JavaClass}
     * @param calleeMethodName 呼出APIメソッド名
     * @param calleeMethodSig 呼出APIシグネチャ
     * @return calleeJavaClassがcalleeMethodNameAndSigで表されるメソッドを有する場合、そのメソッド情報を保持する{@link Method}
     */
    private static Method getMethodOf(JavaClass calleeJavaClass, String calleeMethodName, String calleeMethodSig) {

        calleeMethodSig = calleeMethodSig.replace('.', '/');
        Method[] methods = calleeJavaClass.getMethods();
        for (Method method : methods) {
            String methodNameAndSig = new StringBuilder(method.getName()).append(method.getSignature()).toString();
            if (methodNameAndSig.startsWith(new StringBuilder(calleeMethodName).append(calleeMethodSig).toString())) {
                return method;
            }
        }
        return null;
    }

    /**
     * 当該のクラスレベルにてメソッドが公開されているかをチェックする。
     *
     * @param calleeJavaClass チェック対象のクラス情報を保持する{@link JavaClass}
     * @param calleeMethodName 呼出APIメソッド名
     * @param calleeMethodSig 呼出APIシグネチャ
     * @return 当該のクラスレベルにてメソッドが公開されている場合{@code true}
     */
    private static boolean checkPublicityForTheClass(JavaClass calleeJavaClass, String calleeMethodName, String calleeMethodSig) {

        String calleeApi = getCalleeApi(calleeJavaClass.getClassName().replace('$', '.'), calleeMethodName, calleeMethodSig);
        if (publishedMethodAndConstructorSet.contains(calleeApi)) {
            return true;
        }

        return checkForClassOrPackage(calleeApi);
    }

    /**
     * クラスレベル・パッケージレベルで公開されているか否かをチェックする。
     * 
     * @param calleeJavaClass チェック対象クラス名
     * @return クラスレベル・パッケージレベルで公開されている場合、{@code true}
     */
    private static boolean checkForClassOrPackage(String calleeJavaClass) {

        String[] packageParts = calleeJavaClass.split("\\.");
        StringBuilder sb = new StringBuilder();
        // 当該のパッケージまたはクラスが公開されているか判定する。
        // 上位のパッケージから判定していき、最終的にクラスが公開されているか判定する。
        for (int i = 0; i < packageParts.length; i++) {
            if (i > 0) {
                sb.append(".");
            }
            sb.append(packageParts[i]);
            if (publishedPackageOrClassSet.contains(sb.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 呼び出されたクラス自身が当該のメソッドを有しない場合、スーパークラスとインタフェースレベルにて公開されているか否かをチェックする。
     * 
     * @param calleeJavaClass 呼び出されたクラスの情報を保持する{@link JavaClass}
     * @param calleeMethodName 呼出APIメソッド名
     * @param calleeMethodSig 呼出APIシグネチャ
     * @return スーパークラスまたはインタフェースレベルにて公開されていれば{@code true}
     * @throws ClassNotFoundException 親クラスの{@link JavaClass}
     * を取得できない場合に発生する。この例外が発生する場合は、クラスパス設定を確認すること。
     */
    private static boolean checkSuperClassOrInterface(JavaClass calleeJavaClass, String calleeMethodName, String calleeMethodSig)
            throws ClassNotFoundException {

        JavaClass[] interfaceJavaClasses = calleeJavaClass.getInterfaces();
        for (JavaClass interfaze : interfaceJavaClasses) {
            if (isPermittedForClassOrInterface(interfaze, calleeMethodName, calleeMethodSig)) {
                return true;
            }
        }

        // インタフェースに対して親クラスのチェックは行わない。
        if (!calleeJavaClass.isInterface()) {
            JavaClass superClass = calleeJavaClass.getSuperClass();
            if (superClass != null && isPermittedForClassOrInterface(superClass, calleeMethodName, calleeMethodSig)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 呼び出されたAPIの名称を得る。<br/>
     * classファイルフォーマットで記述されたパラメータを設定ファイルの形式に合うよう、Javaファイルフォーマットに変換する。
     * 
     * @param calleeClassName 呼び出されたAPIのクラス名
     * @param calleeMethodName 呼出APIメソッド名
     * @param calleeMethodSig 呼出APIシグネチャ
     * @return Javaファイルフォーマットにて記述された被呼出API
     */
    static String getCalleeApi(String calleeClassName, String calleeMethodName, String calleeMethodSig) {

        String parameter = calleeMethodSig.substring(calleeMethodSig.indexOf("(") + 1, calleeMethodSig.lastIndexOf(")"));

        // コンストラクタ対応
        if ("<init>".equals(calleeMethodName)) {
            if (calleeClassName.indexOf("$") != -1) {
                calleeMethodName = calleeClassName.substring(calleeClassName.lastIndexOf("$") + 1);
            } else {
                calleeMethodName = calleeClassName.substring(calleeClassName.lastIndexOf(".") + 1);
            }
        }

        StringBuilder calleeApi = new StringBuilder(calleeClassName);
        calleeApi.append(".");
        calleeApi.append(calleeMethodName);
        calleeApi.append(getParsedParameter(parameter));
        return calleeApi.toString();
    }

    /**
     * Classファイルのフォーマットで記述されているパラメータをJavaファイルのファーマットに変換する。<br>
     * 変換ルールはJava仮想マシン仕様 4.3.2「フィールド・ディスクリプタ」を参照。
     * 
     * <pre>
     * 例：)
     * Ljava.lang.String;B[ID → (java.lang.String,byte,int[],double)
     * Ljava.io.File;[[JSS    → (java.io.File,long[][],short,short)
     * </pre>
     * 
     * Classファイルのフォーマットとの対応は、下記のとおりである。
     * 
     * <pre>
     * L"classname"; : クラスclassnameのインスタンス
     * [             : 配列次元（1次元分）複数次元の場合は次元数分"["を記述する。
     * B             : byte
     * C             : char
     * D             : double
     * F             : float
     * I             : int
     * J             : long
     * S             : short
     * Z             : boolean
     * </pre>
     * 
     * @param beforeParameter
     *            クラスファイルフォーマットのパラメータ
     * @return Javaファイルフォーマットのパラメータ
     */
    private static String getParsedParameter(String beforeParameter) {

        StringBuilder parameter = new StringBuilder("(");
        beforeParameter = beforeParameter.replace('/', '.');
        boolean inArray = false;
        int arrayCount = 0;

        for (int i = 0; i < beforeParameter.length(); i++) {

            if (i != 0 && !inArray) {
                parameter.append(",");
            }

            switch (beforeParameter.charAt(i)) {
            case 'B':
                parameter.append("byte");
                break;
            case 'C':
                parameter.append("char");
                break;
            case 'D':
                parameter.append("double");
                break;
            case 'F':
                parameter.append("float");
                break;
            case 'I':
                parameter.append("int");
                break;
            case 'J':
                parameter.append("long");
                break;
            case 'L':
                int refenceEnd = beforeParameter.indexOf(";", i);
                String reference = beforeParameter.substring(i + 1, refenceEnd);
                i = refenceEnd;
                parameter.append(reference);
                break;
            case 'S':
                parameter.append("short");
                break;
            case 'Z':
                parameter.append("boolean");
                break;
            case '[':
                inArray = true;
                arrayCount++;
                break;
            default:
            }

            if (inArray && beforeParameter.charAt(i) != '[') {
                for (int j = 0; j < arrayCount; j++) {
                    parameter.append("[]");
                }
                inArray = false;
                arrayCount = 0;
            }

        }
        parameter.append(")");
        return parameter.toString();
    }
}
