package nablarch.test.tool.findbugs;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.config.UserPreferences;
import edu.umd.cs.findbugs.test.AnalysisRunner;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.test.tool.findbugs.PublishedApisInfoTest.AbnormalSuite;
import nablarch.test.tool.findbugs.PublishedApisInfoTest.NormalSuite;
import nablarch.test.tool.findbugs.PublishedApisInfoTest.UsageOfUnpublishedMethodDetector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.io.BufferedReader;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * {@link PublishedApisInfo}のテスト
 * 使用しているfindbugsが1.3.9のため、java8には対応しておらずエラーが出ます。
 * java6、もしくはjava7で実行してください。
 * また、テストを実行する際はgradleのtestタスクで実行してください。
 *
 * @author 香川朋和
 */
// 順序によってテストが失敗する場合があるので、順序を明示的に指定。
@RunWith(Suite.class)
@SuiteClasses({NormalSuite.class, AbnormalSuite.class, UsageOfUnpublishedMethodDetector.class})
public class PublishedApisInfoTest {

    private static final String CONFIG_FILE_PATH = "nablarch-findbugs-config";

    /**
     * 正常系のテストケース。
     * （こちらが先に実行されないとテストが失敗する。）
     */
    public static class NormalSuite {

        /**
         * コンフィグファイルに何も記述されていない場合、
         * すべてのクラスで使用不許可となる
         */
        @Test
        public void testReadConfigs1File0Record() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting0record");

            PublishedApisInfo.readConfigFiles();
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));

            //カバレッジ用に、スーパークラスを持たない、非公開なクラスを読み込ませる
            Assert.assertFalse(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.setting.method.unpublishedpackage.UnpublishedPackage",
                    "unpublishedPackageTest1", "()V"));
        }

        /**
         * 指定したディレクトリにコンフィグファイルが存在しない場合。
         * この場合もすべてのクラスが使用不許可となる。
         */
        @Test
        public void testReadConfigsNoFile() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/nosettings");
            PublishedApisInfo.readConfigFiles();
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
        }

        /**
         * {@link nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass#testMethod()}
         * のみコンフィグファイルに記述されている場合。
         */
        @Test
        public void testReadConfigs1File1Record() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting1record");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
        }

        /**
         * {@link nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass#testMethod()}と
         * {@link nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass#testMethod2()}が
         * コンフィグファイルに記述されている場合。
         */
        @Test
        public void testReadConfigs1File2Record() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting2record");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
        }

        /**
         * Innerクラスの場合のケース
         * Innerクラスのコンストラクタ、メソッドを確認する。
         */
        @Test
        public void testInnerClass() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/innerClass");
            PublishedApisInfo.readConfigFiles();

            // 許可リストに定義されているInnerクラス。
            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "([Ljava/lang/String;)V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "isHoge", "()" +
                            "boolaen"));

            // 許可リストに定義されていないInnerクラス。
            Assert.assertFalse(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$NG", "<init>", "()V"));
        }

        /**
         * Innerクラスの場合のケース
         * パッケージに対して使用許可がある場合、Innerクラスも使用許可となること。
         */
        @Test
        public void testInnerClass2() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/innerClass2");
            PublishedApisInfo.readConfigFiles();

            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$OK", "<init>", "([Ljava/lang/String;)V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass$NG", "<init>", "()V"));
        }

        /**
         * 指定したディレクトリ直下にコンフィグファイルが複数ある場合、
         * すべてのコンフィグファイルが読み込めること。
         */
        @Test
        public void testReadConfigs2Files() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/twosettings");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings" +
                    ".data.java.TestClass", "testMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings" +
                    ".data.java.TestClass", "testMethod3", "()V"));
        }

        /**
         * パッケージを指定すると、そのパッケージに存在するクラスのメソッドはすべて
         * 使用許可となること。
         */
        @Test
        public void testReadConfigsPackage() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/packaze");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
        }

        /**
         * クラスを指定すると、そのクラスに存在するクラスのメソッドはすべて
         * 使用許可となること。
         * 暗黙的に継承しているjava.lang.Objectのメソッドは使用不許可となること。
         */
        @Test
        public void testReadConfigClass() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/class");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod2", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod3", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "toString", "()V"));
        }

        /**
         * 1つのクラスを継承している場合のケース
         * スーパークラスを指定すると、コンフィグファイルに記述はなくても継承元のメソッドは使用許可となること。
         * ただし、オーバーライドされたメソッドをサブクラスから呼び出す場合は、使用不許可となること。
         */
        @Test
        public void testIsPermittedSuperClass() {
            System.setProperty(CONFIG_FILE_PATH,
                    "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/superclass");
            PublishedApisInfo.readConfigFiles();
            // スーパークラスとして宣言した場合（実装イメージ：Super superinstance = new Sub()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "superMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "superOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "toString", "()V"));

            // サブクラスとして宣言した場合（実装イメージ：Sub subinstance = new Sub()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "superMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "superOnlyMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "subOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "toString", "()V"));
        }

        /**
         * 1つのクラスを継承している場合のケース
         * サブクラスを指定すると、オーバーライドされたメソッドをサブクラスから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていない継承元のメソッドはは、使用不許可となること。
         */
        @Test
        public void testIsPermittedSubClass() {
            System.setProperty(CONFIG_FILE_PATH,
                    "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subclass");
            PublishedApisInfo.readConfigFiles();
            // スーパークラスとして宣言した場合（実装イメージ：Super superinstance = new Sub()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "superMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "superOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "toString", "()V"));

            // サブクラスとして宣言した場合（実装イメージ：Sub subinstance = new Sub()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "superMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "superOnlyMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "subOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "toString", "()V"));
        }

        /**
         * 1つのInterfaceを実装している場合のケース
         * Interfaceを指定すると、コンフィグファイルに記述はなくてもInterfaceのメソッドは使用許可となること。
         * ただし、オーバーライドされた実装クラスのメソッドから呼び出す場合は、使用不許可となること。
         */
        @Test
        public void testIsPermittedOneInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/oneinterface");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：OneInterfaceImple oneinterfaceimple = new OneInterfaceImple()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "interfaceMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "interfaceDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "interfaceOnlyDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "impleOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "toString", "()V"));

            // インタフェースとして宣言した場合（実装イメージ：InterfaceFor1Interface oneinterface = new OneInterfaceImple()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceOnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "toString", "()V"));
        }

        /**
         * 1つのInterfaceを実装している場合のケース
         * 実装クラスを指定すると、オーバーライドされたメソッドを実装クラスから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterfaceのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedOneInterfaceImple() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/oneinterfaceimple");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：OneInterfaceImple oneinterfaceimple = new OneInterfaceImple()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "interfaceMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "interfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "interfaceOnlyDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "impleOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "toString", "()V"));

            // インタフェースとして宣言した場合（実装イメージ：InterfaceFor1Interface oneinterface = new OneInterfaceImple()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceOnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "toString", "()V"));
        }

        /**
         * 1つのInterfaceを継承したInterfaceを実装している場合のケース
         * SuperInterfaceを指定すると、オーバーライドされたメソッドをSuperInterfaceから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterfaceのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedSuperInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/superinterface");
            PublishedApisInfo.readConfigFiles();
            // SuperInterfaceと実装クラスを宣言する場合は、ツールの仕様的（Interface、クラスを見つかるまで探しに行く）に１つのInterfaceを実装している場合と同義のため省略している。

            // SubInterfaceとして宣言した場合（実装イメージ：SubInterface subinterface = new SubInterfaceImple()）
            // subInterfaceMethod、subInterfaceonlyDefaultMethodの呼び出しについては、１つのInterfaceを実装している場合と同義のため省略している。
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "superInterfaceDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "superInterfaceMethod", "()V"));

        }

        /**
         * 1つのInterfaceを継承したInterfaceを実装している場合のケース
         * SubInterfaceを指定すると、オーバーライドされたメソッドをSubInterfaceから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterfaceのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedSubInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface");
            PublishedApisInfo.readConfigFiles();
            // SuperInterfaceと実装クラスを宣言する場合は、ツールの仕様的（Interface、クラスを見つかるまで探しに行く）に１つのInterfaceを実装している場合と同義のため省略している。

            // SubInterfaceとして宣言した場合（実装イメージ：SubInterface subinterface = new SubInterfaceImple()）
            // subInterfaceMethod、subInterfaceonlyDefaultMethodの呼び出しについては、１つのInterfaceを実装している場合と同義のため省略している。
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "superInterfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "superInterfaceMethod", "()V"));

        }

        /**
         * 1つのInterfaceを継承したInterfaceを実装している場合のケース
         * 実装クラスを指定すると、オーバーライドされたメソッドを実装クラスから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterfaceのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedSubInterfaceImple() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterfaceimple");
            PublishedApisInfo.readConfigFiles();
            // SuperInterfaceと実装クラスを宣言する場合は、ツールの仕様的（Interface、クラスを見つかるまで探しに行く）に１つのInterfaceを実装している場合と同義のため省略している。

            // SubInterfaceとして宣言した場合（実装イメージ：SubInterface subinterface = new SubInterfaceImple()）
            // subInterfaceMethod、subInterfaceonlyDefaultMethodの呼び出しについては、１つのInterfaceを実装している場合と同義のため省略している。
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "superInterfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "superInterfaceMethod", "()V"));

        }

        /**
         * 2つのInterfaceを実装している場合のケース
         * 片方のInterfaceを指定すると、コンフィグファイルに記述はなくてもそのInterfaceのメソッドは使用許可となること。
         * ただし、オーバーライドされた実装クラスのメソッドから呼び出す場合は、使用不許可となること。
         */
        @Test
        public void testIsPermittedTwoInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/twointerface");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：TwoInterfaceImple twointerfaceimple = new TwoInterfaceImple()）
            // もう片方のInterface（InterfaceFor1Interface）のメソッドの呼び出しについては、以下のInterfaceFor2Interfaceを呼び出す場合と同義のため省略している。
            // twoInterfaceImpleOnlyMethodの呼び出しについては、１つのInterfaceを実装している場合と同義のため省略している。
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2Method", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2DefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2OnlyDefaultMethod", "()V"));

            // 片方のInterfaceとして宣言した場合（実装イメージ：InterfaceFor2Interface twointerface = new TwoInterfaceImple()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2Method", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2DefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2OnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "toString", "()V"));


        }

        /**
         * 2つのInterfaceを実装している場合のケース
         * 片方のInterfaceを指定しても、もう片方のIntefaceのメソッドは使用不許可となること。
         */
        @Test
        public void testIsNotPermittedTwoInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/oneinterface");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：TwoInterfaceImple twointerfaceimple = new TwoInterfaceImple()）
            // もう片方のInterface（InterfaceFor1Interface）のメソッドの呼び出しについては、以下のInterfaceFor2Interfaceを呼び出す場合と同義のため省略している。
            // twoInterfaceImpleOnlyMethodの呼び出しについては、１つのInterfaceを実装している場合と同義のため省略している。
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2Method", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2DefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2OnlyDefaultMethod", "()V"));

            // 片方のInterfaceとして宣言した場合（実装イメージ：InterfaceFor2Interface twointerface = new TwoInterfaceImple()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2Method", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2DefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2OnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "toString", "()V"));


        }


        /**
         * 2つのInterfaceを実装している場合のケース
         * 実装クラスを指定すると、オーバーライドされたメソッドを実装クラスから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterfaceのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedTwoInterfaceImple() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/twointerfaceimple");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：TwoInterfaceImple twointerfaceimple = new TwoInterfaceImple()）
            // もう片方のInterface（InterfaceFor1Interface）のメソッドの呼び出しについては、以下のInterfaceFor2Interfaceを呼び出す場合と同義のため省略している。
            // twoInterfaceImpleOnlyMethodの呼び出しについては、１つのInterfaceを実装している場合と同義のため省略している。
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2Method", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2DefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.TwoInterfaceImple", "interface2OnlyDefaultMethod", "()V"));

            // 片方のInterfaceとして宣言した場合（実装イメージ：InterfaceFor2Interface twointerface = new TwoInterfaceImple()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2Method", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2DefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "interface2OnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor2Interface", "toString", "()V"));


        }

        /**
         * クラスが1つのクラスを継承するかつ、1つのInterfaceを実装している場合のケース
         * Interfaceを指定すると、コンフィグファイルに記述はなくてもそのInterfaceのメソッドは使用許可となること。
         * ただし、オーバーライドされた実装クラスのメソッドから呼び出す場合は、使用不許可となること。
         */
        @Test
        public void testIsPermittedImplementedInterfaceWithSuperClass() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/oneinterface");
            PublishedApisInfo.readConfigFiles();

            // 実装クラスとして宣言した場合（実装イメージ：InterfaceImpleWithSuper interfaceimplewithsuper = new InterfaceImpleWithSuper()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceOnlyDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "superMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "superOnlyMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "impleOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "toString", "()V"));
        }

        /**
         * クラスが1つのクラスを継承するかつ、1つのInterfaceを実装している場合のケース
         * スーパークラスを指定すると、コンフィグファイルに記述はなくても継承元のメソッドは使用許可となること。
         * ただし、オーバーライドされたメソッドをサブクラスから呼び出す場合は、使用不許可となること。
         */
        @Test
        public void testIsPermittedSuperClassWithImplementedInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/superclass");
            PublishedApisInfo.readConfigFiles();

            // 実装クラスとして宣言した場合（実装イメージ：InterfaceImpleWithSuper interfaceimplewithsuper = new InterfaceImpleWithSuper()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceOnlyDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "superMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "superOnlyMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "impleOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "toString", "()V"));
        }

        /**
         * クラスが1つのクラスを継承するかつ、1つのInterfaceを実装している場合のケース
         * 実装クラスを指定すると、オーバーライドされたメソッドを実装クラスから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterface、スーパークラスのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedClassWithSuperClassAndInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/superclassandinterface");
            PublishedApisInfo.readConfigFiles();

            // 実装クラスとして宣言した場合（実装イメージ：InterfaceImpleWithSuper interfaceimplewithsuper = new InterfaceImpleWithSuper()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "interfaceOnlyDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "superMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "superOnlyMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "impleOnlyMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceImpleWithSuper", "toString", "()V"));
        }

        /**
         * メソッドのシグネチャを正しく読み込めていること。
         */
        @Test
        public void testIsPermittedParameterConvert() {
            System.setProperty(CONFIG_FILE_PATH,
                    "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/parameter/convert");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo
                    .isPermitted(
                            "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.parameter.convert.ParameterConvert",
                            "testParameterConvert",
                            "(Ljava.lang.String;IJBSCFDZ[Ljava.lang.String;[I[J[B[S[C[F[D[Z[[Ljava.lang.String;[[I[[J[[B[[S[[C[[F[[D[[Z)V"));
        }

        /**
         * privateなメソッドはコンフィグファイルに記述がなくても
         * trueが返ること
         */
        @Test
        public void testIsPermittedPrivateMethod() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/");
            PublishedApisInfo.readConfigFiles();
            Assert.assertTrue(PublishedApisInfo.isPermitted(
                    "nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "privateMethod",
                    "()V"));
        }

        /**
         * 全てのクラスが暗黙的に継承しているjava.lang.Objectに対して、
         * コンフィグファイルに記述すると使用許可となること。
         */
        @Test
        public void testIsPermittedObjectMethod() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/object");
            PublishedApisInfo.readConfigFiles();
            // 単体のクラスの場合
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "toString", "()V"));
            // 許可リストに定義されていないクラス。
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.TestClass", "testMethod", "()V"));

            // クラスを継承したサブクラスの場合
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super", "toString", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Sub", "toString", "()V"));

            // インタフェースを実装したクラスの場合
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.OneInterfaceImple", "toString", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "toString", "()V"));

            // １つのインタフェースを継承したインターフェースを実装したクラスの場合
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface", "toString", "()V"));

            // Enumの場合
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestEnum", "toString", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "toString", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestEnum", "toString", "()V"));
        }

        /**
         * 1つのInterfaceを実装しているEnumの場合のケース
         * Interfaceを指定すると、コンフィグファイルに記述はなくてもInterfaceのメソッドは使用許可となること。
         * ただし、オーバーライドされた実装クラスのメソッドから呼び出す場合は、使用不許可となること。
         */
        @Test
        public void testIsPermittedImplementedInterfaceWithEnum() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/oneinterface");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：TestImpleEnum testenum = new TestImpleEnum()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "interfaceMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "interfaceDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "interfaceOnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "toString", "()V"));

            // インタフェースとして宣言した場合（実装イメージ：InterfaceFor1Interface oneinterface = new TestImpleEnum()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceDefaultMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceOnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "toString", "()V"));

        }

        /**
         * 1つのInterfaceを実装しているEnumの場合のケース
         * Enumを指定すると、オーバーライドされたメソッドを実装クラスから呼ぶ場合は使用許可となること。
         * ただし、オーバーライドされていないInterfaceのメソッドは、使用不許可となること。
         */
        @Test
        public void testIsPermittedEnumWithInterface() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/enumz");
            PublishedApisInfo.readConfigFiles();
            // 実装クラスとして宣言した場合（実装イメージ：TestImpleEnum testenum = new TestImpleEnum()）
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "interfaceMethod", "()V"));
            Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "interfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "interfaceOnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz.TestImpleEnum", "toString", "()V"));

            // インタフェースとして宣言した場合（実装イメージ：InterfaceFor1Interface oneinterface = new TestImpleEnum()）
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceDefaultMethod", "()V"));
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "interfaceOnlyDefaultMethod", "()V"));
            // 暗黙的に継承しているObjectメソッドは使用不許可となること
            Assert.assertFalse(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface", "toString", "()V"));

        }
    }

    /**
     * 異常系テストケース。
     * こちらを後に実行しないと、{@link PublishedApisInfo}のstatic initializerでエラーになる。
     */
    public static class AbnormalSuite {

        /**
         * 読み込むJavaクラスが見つからない場合
         */
        @Test
        public void testIsPermittedNoExistingClass() {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface");

            PublishedApisInfo.readConfigFiles();
            try {
                Assert.assertTrue(PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.NoExistingClass",
                        "superInterfaceMethod", "()V"));
            } catch (RuntimeException e) {
                Assert.assertEquals(
                        "Couldn't find JavaClass of itself or super class. ClassName=[nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.NoExistingClass]",
                        e.getMessage());
            }
        }

        /**
         * 指定された設定ディレクトリが存在しない場合、例外が発生すること。
         * また、例外のメッセージから、設定の問題箇所を判断できること。
         */
        @Test
        public void testReadConfigFiles_NotExistingDirectory() {
            try {
                System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configured/notExistingDirectory");
                PublishedApisInfo.readConfigFiles();
                fail();
            } catch (RuntimeException e) {
                assertThat(e.getMessage(), containsString("Config file directory doesn't exist"));
                assertThat(e.getMessage(), containsString("src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configured/notExistingDirectory"));
            }
        }

        /**
         * 設定ファイルのディレクトリが設定されていなかった場合、
         * {@link System#getProperty(String)}が設定する例外が発生すること。
         */
        @Test
        public void testReadConfigNoSettingDirectory() {
            try {
                System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/notexsitingdir");
                PublishedApisInfo.readConfigFiles();
            } catch (RuntimeException e) {
                Assert.assertEquals("Config file directory doesn't exist.Path=[src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/notexsitingdir]", e.getMessage());
            }
        }

        /**
         * 指定されたパスがファイルだった場合、例外が発生すること。
         * また、例外のメッセージから、設定の問題箇所を判断できること。
         */
        @Test
        public void testReadConfigDirectory() {
            try {
                System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/expected/settingsTest.txt");
                PublishedApisInfo.readConfigFiles();
            } catch (RuntimeException e) {
                Assert.assertEquals("Config file directory doesn't exist.Path=[src/test/java/nablarch/test/tool/findbugs/expected/settingsTest.txt]", e.getMessage());
            }
        }

        private static final String FS = File.separator;

        /**
         * 設定ファイル読み込み中にIOExceptionが発生した場合、例外が発生すること。
         * また、例外のメッセージから、設定の問題箇所を判断できること。
         */
        @Test
        public void testReadConfigFiles_IOException(@Mocked final BufferedReader reader) throws IOException {
            new Expectations() {{
                reader.readLine();
                result = new IOException();
            }};
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface");
            try {
                PublishedApisInfo.readConfigFiles();
                fail();
            } catch (RuntimeException e) {
                assertThat(e.getMessage(), containsString("Couldn't read config file."));
                assertThat(e.getMessage(), containsString(new File("src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface").toString()));
            }
        }
    }


    public static class UsageOfUnpublishedMethodDetector {

        private static final String CONFIG_FILE_PATH = "nablarch-findbugs-config";

        private final AnalysisRunner runner = new AnalysisRunner();

        @Before
        public void setUpSpotBugsRule() {
            runner.addAuxClasspathEntry(Paths.get("src/test/java/nablarch/test/tool/findbugs/data/jsrbin/"));
            runner.addAuxClasspathEntry(Paths.get("src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/"));
        }

        /**
         * 以下の動作を確認する。
         * ・コンストラクタ、メソッド単位で公開非公開を設定した場合
         *   ・公開指定されたコンストラクタ、メソッド以外を出力
         *   ・シグネチャが違えば別の要素として判断
         * ・パッケージ単位で設定された場合
         *   ・公開設定されたパッケージのクラス、サブパッケージも公開される
         * ・クラス単位で設定された場合
         *   ・公開されたクラスのメソッドはすべて公開される
         *   ・内部クラス、抽象クラス、インターフェースに対しても普通のクラスと同じ動作をする
         *   ・無名クラス内部は検知されない。
         *
         * @throws Exception
         */
        @Test
        public void testSettings() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/settings/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/settingTest.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/settings/Caller.class");
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
         * @throws Exception
         */
        @Test
        public void testMethodCall() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallTest.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/Caller.class");
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
         * @throws Exception
         */
        @Test
        public void testMethodCallInNonMethod() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInNonMethodTest.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInNonMethodTest.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * 無名クラスを読み込ませた際の動作を確認する。
         */
        @Test
        public void testMethodCallInAnonymousClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInAnnonymousClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation$1.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInAnnonymousClass.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * ローカルクラスを読み込ませた際の動作を確認する。
         * @throws Exception
         */
        @Test
        public void testMethodCallInLocalClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInLocalClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation$1Local.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInLocalClass.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * 内部クラスを読み込ませた際の動作を確認する。
         * @throws Exception
         */
        @Test
        public void testMethodCallInInnerClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInInnerClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/ClassForVariousLocation$Inner.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/methodCallInInnerClass.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * サブクラスを読み込ませた際の動作を確認する。
         *
         * @throws Exception
         */
        @Test
        public void testMethodCallInSubClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/methodcall/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/methodCallInSubClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/methodcall/inherit/method/ClassC.class");
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
        public void testExceptionsTopLevelClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionTopLevelClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionTopLevelClass.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * 内部クラスで使用されている例外クラスに対する動作の確認を行う。
         *
         * @throws Exception
         */
        @Test
        public void testExceptionsInnerClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionInnerClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller$InnerClass.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionInnerClass.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * ローカルクラスで使用されている例外クラスに対する動作の確認を行う。
         *
         * @throws Exception
         */
        @Test
        public void testExceptionsLocalClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionTest.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller$1LocalClass.class");
            assertFiles("src/test/java/nablarch/test/tool/findbugs/expected/exceptionLocalClass.txt", outputFile);
            deleteFile(outputFile);
        }

        /**
         * 無名クラスで使用されている例外クラスに対する動作の確認を行う。
         *
         * @throws Exception
         */
        @Test
        public void testExceptionsAnonymousClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionAnnonymousClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/Caller$1.class");
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
        public void testExceptionsJsrMode() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionJsrMode.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/jsrbin/nablarch/test/tool/findbugs/data/exception/Caller.class");
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
        public void testExceptionsJava6() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionJaba6.txt";
            doFindBugs(outputFile,
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
        public void testInnerExceptionClass() throws Exception {
            System.setProperty(CONFIG_FILE_PATH, "src/test/java/nablarch/test/tool/findbugs/data/exception/settings2");
            PublishedApisInfo.readConfigFiles();

            String outputFile = "src/test/java/nablarch/test/tool/findbugs/exceptionInnerExceptionClass.txt";
            doFindBugs(outputFile, "src/test/java/nablarch/test/tool/findbugs/data/notjsrmode/nablarch/test/tool/findbugs/data/exception/CallerForExceptionInnerClass.class");
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
        private void doFindBugs(String outputFile, String classForCheck) throws IOException {
            Path path = Paths.get(classForCheck);
            Path output = Paths.get(outputFile);

            // Java21対応のためSpotBugsのバージョンを4.8.3に上げたところ、testMethodCall実行時にMethodReturnCheckでIllegalArgumentExceptionが発生してしまう。
            // 原因は不明だが、MethodReturnCheckはテストに無関係のため、無効にすることでエラーを回避している。
            BugCollection bugCollection = runner.run((engine) -> {
                DetectorFactoryCollection detectorFactoryCollection = DetectorFactoryCollection.instance();
                DetectorFactory factory = detectorFactoryCollection.getFactory("MethodReturnCheck");
                UserPreferences userPreferences = engine.getUserPreferences();
                userPreferences.enableDetector(factory, false);
            } ,path).getBugCollection();


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
        private static final Comparator<BugInstance> BY_START_LINE
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
         * @param actualFilePath 実際のファイルパス
         * @throws IOException ファイル入出力の際に発生した例外
         */
        private void assertFiles(String expectedFilePath, String actualFilePath) throws IOException {
            String expectedString = getStringFromFile(expectedFilePath);
            String actualString = getStringFromFile(actualFilePath);
            Assert.assertEquals(expectedString, actualString);
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
}
