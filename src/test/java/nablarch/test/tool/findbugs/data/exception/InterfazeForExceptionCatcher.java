package nablarch.test.tool.findbugs.data.exception;

public interface InterfazeForExceptionCatcher {
    public void testLocalClassException(boolean test1) throws PublishedException1, UnpublishedException1;

    public void testExceptionCatcher(boolean test2);
}
