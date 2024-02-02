package nablarch.test.tool.findbugs;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.test.SpotBugsExtension;
import edu.umd.cs.findbugs.test.SpotBugsRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static nablarch.test.Assertion.fail;

@ExtendWith(SpotBugsExtension.class)
public class PublishedApisInfoDetectorTest {

    private static final String CONFIG_FILE_PATH = "nablarch-findbugs-config";
    private SpotBugsRunner spotbugs;


    @BeforeEach
    public void setUpSpotBugsRule(SpotBugsRunner spotbugs) {
        spotbugs.addAuxClasspathEntry(Paths.get("src/test/java/nablarch/test/tool/findbugs/data/jsrbin/"));
        spotbugs.addAuxClasspathEntry(Paths.get("src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/"));
        this.spotbugs = spotbugs;
    }

    /**
     * 以下の動作を確認する。
     * ・コンストラクタ、メソッド単位で公開非公開を設定した場合
     * ・公開指定されたコンストラクタ、メソッド以外を出力
     * ・シグネチャが違えば別の要素として判断
     * ・パッケージ単位で設定された場合
     * ・公開設定されたパッケージのクラス、サブパッケージも公開される
     * ・クラス単位で設定された場合
     * ・公開されたクラスのメソッドはすべて公開される
     * ・内部クラス、抽象クラス、インターフェースに対しても普通のクラスと同じ動作をする
     * ・無名クラス内部は検知されない。
     *
     * @throws Exception
     */
    @Test
    public void testSettings(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/settings/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/settingTest.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/settings/Caller.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/settingsTest.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 各種構文中で使用されるAPIに対する動作を確認する。
     * 確認している構文は以下の通り
     * ・メソッドの使用
     * ・メソッドチェイン
     * ・if文
     * ・for文
     * ・while文
     * ・do-while文
     * ・switch文
     * ・三項演算子
     * ・return文
     * ・catch文、finally文
     * ・継承
     * ・インターフェース
     *
     * @throws Exception
     */
    @Test
    public void testMethodCall(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallTest.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/Caller.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallTest.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 様々な位置でのメソッド、コンストラクタ呼び出し時の動作を確認する。
     * 以下の内部での動作を確認する。
     * ・静的初期化子
     * ・インスタンス初期化子
     * ・コンストラクタ
     * ・無名クラス
     * ・ローカルクラス
     * ・継承有ローカルクラス
     * ・内部クラス
     *
     * @throws Exception
     */
    @Test
    public void testMethodCallInNonMethod(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInNonMethodTest.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInNonMethodTest.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 無名クラスを読み込ませた際の動作を確認する。
     */
    @Test
    public void testMethodCallInAnonymousClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInAnnonymousClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation$1.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInAnnonymousClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * ローカルクラスを読み込ませた際の動作を確認する。
     *
     * @throws Exception
     */
    @Test
    public void testMethodCallInLocalClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInLocalClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation$1Local.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInLocalClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 内部クラスを読み込ませた際の動作を確認する。
     *
     * @throws Exception
     */
    @Test
    public void testMethodCallInInnerClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInInnerClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation$Inner.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInInnerClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * サブクラスを読み込ませた際の動作を確認する。
     *
     * @throws Exception
     */
    @Test
    public void testMethodCallInSubClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInSubClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/inherit/method/ClassC.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInSubClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 例外クラスに対する動作の確認を行う。
     * それぞれの位置に書かれた例外クラスに対して、正しく検査できることを確認する。
     * ・静的初期化子中
     * ・インスタンス初期化子中
     * ・トップレベルクラスthrows指摘
     * ・トップレベルクラス中catch指摘
     * ・catch句内のネストしたtry-catch
     *
     * @throws Exception
     */
    @Test
    public void testExceptionsTopLevelClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionTopLevelClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionTopLevelClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 内部クラスで使用されている例外クラスに対する動作の確認を行う。
     *
     * @throws Exception
     */
    @Test
    public void testExceptionsInnerClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionInnerClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller$InnerClass.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionInnerClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * ローカルクラスで使用されている例外クラスに対する動作の確認を行う。
     *
     * @throws Exception
     */
    @Test
    public void testExceptionsLocalClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionTest.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller$1LocalClass.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionLocalClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 無名クラスで使用されている例外クラスに対する動作の確認を行う。
     *
     * @throws Exception
     */
    @Test
    public void testExceptionsAnonymousClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionAnnonymousClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller$1.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionAnnonymousClass.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * Java1.4以前の、オペコードにjsrが使用されている
     * classファイルに対する動作の確認を行う。
     *
     * @throws Exception
     */
    @Test
    public void testExceptionsJsrMode(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionJsrMode.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/jsrbin/nablarch/test/tool/findbugs/data/exception/Caller.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionJsrMode.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * Java1.6でコンパイルされた
     * classファイルに対する動作の確認を行う。
     *
     * @throws Exception
     */
    @Test
    public void testExceptionsJava6(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionJaba6.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/compilejava1.6/nablarch/test/tool/findbugs/data/exception/Caller.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionJava6.txt", outputFile);
        deleteFile(outputFile);
    }

    /**
     * 例外クラス(内部クラス)がthrows句、catch句に定義されているときの動作を確認する.
     * <ul>
     * <li>
     * 使用が許可された例外クラス(内部クラス)がthrows句、catch句に定義されているとき、
     * Findbugsが使用が禁止されたクラスとして検知しないことを確認する.
     * </li>
     * <li>
     * 使用が許可されない例外クラス（内部クラス）がthrows句、catch句に定義されているとき、
     * Findbugsが使用が禁止されたクラスとして検知することを確認する.
     * </li>
     * </ul>
     */
    @Test
    public void testInnerExceptionClass(SpotBugsRunner spotbugs) throws Exception {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings2");
        PublishedApisInfo.readConfigFiles();

        String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionInnerExceptionClass.txt";
        doFindBugs(spotbugs, outputFile,
            "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/CallerForExceptionInnerClass.class");
        assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionInnerExceptionClass.txt", outputFile);
        deleteFile(outputFile);
    }

    private void deleteFile(String outputFile) {
        File file = new File(outputFile);
        if (!file.delete()) {
            fail();
        }
    }

    /**
     * FindBugsの実行を行う。
     *
     * @throws IOException 処理実行中の例外
     */
    private void doFindBugs(SpotBugsRunner spotbugs, String outputFile, String classForCheck) throws IOException {
        Path path = Paths.get(classForCheck);
        Path output = Paths.get(outputFile);

        BugCollection bugCollection = this.spotbugs.performAnalysis(path);

        // BugCollectionのassertはテキストファイルに出力して期待されるテキストファイルとの検証を行っている。
        // 本来であればBugCollectionに対してassertするようなコードを書くべきであるが、過去資産を流用するため
        // テキストファイルでの検証としている。
        // 元々FindBugsで実装されていた単体テストで-outputオプションで出力したテキストファイルの検証を行っており
        // その検証に用意されていた期待されるテキストファイルをSpotBugs版の単体テストに流用するためにこのような手法を取っている。
        // 過去資産についてはnablarch-unpublished-api-checker-findbugsのリポジトリ参照。
        try (final BufferedWriter writer = Files.newBufferedWriter(output)) {
            StreamSupport.stream(bugCollection.spliterator(), false)
                .filter(this::isUnpublishedApiUsage)
                .sorted(BY_START_LINE)
                .map(this::formatBugInstance)
                .forEach(writeTo(writer));
        }
    }

    private boolean isUnpublishedApiUsage(BugInstance bugInstance) {
        return "UPU_UNPUBLISHED_API_USAGE".equals(bugInstance.getType());
    }

    /** FindBugs版と出力順序が異なっていたためソースコード上の行番号でソート */
    private final Comparator<BugInstance> BY_START_LINE
        = Comparator.comparing(bugInstance -> bugInstance.getAnnotations()
        .stream()
        .filter(it -> it instanceof SourceLineAnnotation)
        .map(it -> (SourceLineAnnotation) it)
        .map(SourceLineAnnotation::getStartLine)
        .findFirst()
        .orElse(0));

    /** FindBugs版の出力に合わせてメッセージをフォーマット */
    private String formatBugInstance(BugInstance bugInstance) {
        return bugInstance.getMessageWithPriorityTypeAbbreviation() +
            "  " +
            bugInstance.getAnnotationsForMessage(true).get(0);
    }

    private Consumer<String> writeTo(BufferedWriter writer) {
        return formattedText -> {
            try {
                writer.write(formattedText);
                writer.write("\r\n");
            } catch (IOException e) {
                fail(e);
            }
        };
    }

    /**
     * ファイルの内容を比較する。
     *
     * @param expectedFilePath 期待する内容が記述されたファイルパス
     * @param actualFilePath   実際のファイルパス
     * @throws IOException ファイル入出力の際に発生した例外
     */
    private void assertFiles(String expectedFilePath, String actualFilePath) throws IOException {
        String expectedString = getStringFromFile(expectedFilePath);
        String actualString = getStringFromFile(actualFilePath);
        Assertions.assertEquals(expectedString, actualString);
    }

    /**
     * filePathにて指定されるファイルの内容を文字列として返却する。
     *
     * @param filePath 取得するファイルのパス
     * @return filePathにて指定されるファイルの内容を文字列としたもの
     * @throws IOException ファイル入出力の際のエラー
     */
    private String getStringFromFile(String filePath) throws IOException {
        return String.join("\r\n", Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8));
    }
}
