package nablarch.test.tool.findbugs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import static nablarch.test.Assertion.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * 異常系テストケース。
 * こちらを後に実行しないと、{@link PublishedApisInfo}のstatic initializerでエラーになる。
 */
public class PublishedApisInfoAbnormalTest {

    private static final String CONFIG_FILE_PATH = "nablarch-findbugs-config";

    @BeforeAll
    static void beforeAll() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/onesetting0record");
        PublishedApisInfo.readConfigFiles();
    }

    /**
     * 読み込むJavaクラスが見つからない場合
     */
    @Test
    public void testIsPermittedNoExistingClass() {
        System.setProperty(CONFIG_FILE_PATH,
            "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface");

        PublishedApisInfo.readConfigFiles();
        try {
            Assertions.assertTrue(
                PublishedApisInfo.isPermitted("nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.NoExistingClass",
                    "superInterfaceMethod", "()V"));
        } catch (RuntimeException e) {
            Assertions.assertEquals(
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
            System.setProperty(CONFIG_FILE_PATH,
                "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configured/notExistingDirectory");
            PublishedApisInfo.readConfigFiles();
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("Config file directory doesn't exist"));
            assertThat(e.getMessage(),
                containsString("src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configured/notExistingDirectory"));
        }
    }

    /**
     * 設定ファイルのディレクトリが設定されていなかった場合、
     * {@link System#getProperty(String)}が設定する例外が発生すること。
     */
    @Test
    public void testReadConfigNoSettingDirectory() {
        try {
            System.setProperty(CONFIG_FILE_PATH,
                "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/notexsitingdir");
            PublishedApisInfo.readConfigFiles();
        } catch (RuntimeException e) {
            Assertions.assertEquals(
                "Config file directory doesn't exist.Path=[src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/configread/notexsitingdir]",
                e.getMessage());
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
            Assertions.assertEquals("Config file directory doesn't exist.Path=[src/test/java/nablarch/test/tool/findbugs/expected/settingsTest.txt]",
                e.getMessage());
        }
    }

    private final String FS = File.separator;

    /**
     * 設定ファイル読み込み中にIOExceptionが発生した場合、例外が発生すること。
     * また、例外のメッセージから、設定の問題箇所を判断できること。
     */
    @Test
    public void testReadConfigFiles_IOException() throws IOException {
        try (final MockedConstruction<BufferedReader> mocked = mockConstruction(BufferedReader.class, (mock, context) -> {
            when(mock.readLine()).thenThrow(new IOException());
        })) {
            System.setProperty(CONFIG_FILE_PATH,
                "src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface");
            try {
                PublishedApisInfo.readConfigFiles();
                fail();
            } catch (RuntimeException e) {
                assertThat(e.getMessage(), containsString("Couldn't read config file."));
                assertThat(e.getMessage(),
                    containsString(new File("src/test/java/nablarch/test/tool/findbugs/data/publishedapi/settings/subinterface").toString()));
            }
        }
    }
}
