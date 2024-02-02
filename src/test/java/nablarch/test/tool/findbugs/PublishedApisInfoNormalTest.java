package nablarch.test.tool.findbugs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 正常系のテストケース。
 * （こちらが先に実行されないとテストが失敗する。）
 */
//@Disabled
public class PublishedApisInfoNormalTest {

    private static final String CONFIG_FILE_PATH = "nablarch-findbugs-config";

    @BeforeAll
    static void beforeAll() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting0record");
        PublishedApisInfo.readConfigFiles();
    }


    /**
     * コンフィグファイルに何も記述されていない場合、
     * すべてのクラスで使用不許可となる
     */
    @Test
    public void testReadConfigs1File0Record() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting0record");

        PublishedApisInfo.readConfigFiles();
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));

        //カバレッジ用に、スーパークラスを持たない、非公開なクラスを読み込ませる
        Assertions.assertFalse(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.setting.method.unpublishedpackage.UnpublishedPackage",
            "unpublishedPackageTest1", "()V"));
    }

    /**
     * 指定したディレクトリにコンフィグファイルが存在しない場合。
     * この場合もすべてのクラスが使用不許可となる。
     */
    @Test
    public void testReadConfigsNoFile() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/nosettings");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
    }

    /**
     * {@link nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass#testMethod()}
     * のみコンフィグファイルに記述されている場合。
     */
    @Test
    public void testReadConfigs1File1Record() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting1record");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
    }

    /**
     * {@link nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass#testMethod()}と
     * {@link nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass#testMethod2()}が
     * コンフィグファイルに記述されている場合。
     */
    @Test
    public void testReadConfigs1File2Record() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting2record");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
    }

    /**
     * Innerクラスの場合のケース
     * Innerクラスのコンストラクタ、メソッドを確認する。
     */
    @Test
    public void testInnerClass() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/innerClass");
        PublishedApisInfo.readConfigFiles();

        // 許可リストに定義されているInnerクラス。
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "()V"));
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "([Ljava/lang/String;)V"));
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "isHoge", "()" +
                "boolaen"));

        // 許可リストに定義されていないInnerクラス。
        Assertions.assertFalse(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$NG", "<init>", "()V"));
    }

    /**
     * Innerクラスの場合のケース
     * パッケージに対して使用許可がある場合、Innerクラスも使用許可となること。
     */
    @Test
    public void testInnerClass2() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/innerClass2");
        PublishedApisInfo.readConfigFiles();

        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "()V"));
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "([Ljava/lang/String;)V"));
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$NG", "<init>", "()V"));
    }

    /**
     * 指定したディレクトリ直下にコンフィグファイルが複数ある場合、
     * すべてのコンフィグファイルが読み込めること。
     */
    @Test
    public void testReadConfigs2Files() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/twosettings");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings" +
            ".data.java.TestClass", "testMethod", "()V"));
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
        Assertions.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings" +
            ".data.java.TestClass", "testMethod3", "()V"));
    }

    /**
     * パッケージを指定すると、そのパッケージに存在するクラスのメソッドはすべて
     * 使用許可となること。
     */
    @Test
    public void testReadConfigsPackage() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/packaze");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
    }

    /**
     * コンフィグファイルに記述されたInterfaceが
     * 使用許可となること。
     */
    @Test
    public void testIsPermitted1Interface() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/oneinterface");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface",
                "test1InterfaceImple", "()V"));
    }

    /**
     * 記述のないインターフェースに対して、使用不許可となること
     */
    @Test
    public void testIsPermittedSuperInterface() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/superinterface");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface",
                "superInterfaceMethod",
                "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface",
                "subInterfaceMethod",
                "()V"));
    }

    /**
     * 別のInterfaceを継承したInterfaceに対して、
     * 継承元のメソッド、自身のメソッドともにコンフィグファイルに記述されたもののみ
     * 使用許可となること。
     */
    @Test
    public void testIsPermittedSubInterface() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface",
            "superPublishedInterfaceMethod", "()V"));
        Assertions.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface",
            "subPublishedInterfaceMethod", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface",
                "superUnpublishedInterfaceMethod", "()V"));
        Assertions.assertFalse(
            PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface",
                "subUnpublishedInterfaceMethod", "()V"));
    }

    /**
     * メソッドのシグネチャを正しく読み込めていること。
     */
    @Test
    public void testIsPermittedParameterConvert() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/parameter/convert");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(PublishedApisInfo
            .isPermitted(
                "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.parameter.convert.ParameterConvert",
                "testParameterConvert",
                "(Ljava.lang.String;IJBSCFDZ[Ljava.lang.String;[I[J[B[S[C[F[D[Z[[Ljava.lang.String;[[I[[J[[B[[S[[C[[F[[D[[Z)V"));
    }

    /**
     * 使用許可のあるクラスを継承したサブクラスで、
     * コンフィグファイルに記述はなくても継承元の許可されたメソッドは
     * 使用できること。
     */
    @Test
    public void testIsPermittedSuperClass() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/superclass");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "testSuper",
            "()V"));
    }

    /**
     * privateなメソッドはコンフィグファイルに記述がなくても
     * trueが返ること
     */
    @Test
    public void testIsPermittedPrivateMethod() {
        System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/");
        PublishedApisInfo.readConfigFiles();
        Assertions.assertTrue(PublishedApisInfo.isPermitted(
            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "privateMethod",
            "()V"));
    }

}
