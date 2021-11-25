package nablarch.test.tool.findbugs.data.exception;

public class Caller {

    // 静的初期化子中
    static {
        try {
            testToplevelClassException(true);
        } catch (PublishedException1 e) {
        } catch (UnpublishedException1 e) {
        }
    }

    // インスタンス初期化子中
    {
        try {
            testToplevelClassException(true);
        } catch (PublishedException1 e) {
        } catch (UnpublishedException1 e) {
        }
    }

    // トップレベルクラスthrows指摘
    public static void testToplevelClassException(boolean test1) throws PublishedException1, UnpublishedException1 {
    }

    // トップレベルクラス中catch指摘
    public void testExceptionCatcher(boolean test2) {

        // ネストしていないtry-catch
        try {
            testToplevelClassException(true);
        } catch (PublishedException1 e) {

            // ネストしたtry-catch
            try {
                if (test2) {
                    throw new PublishedException2();
                } else {
                    throw new UnpublishedException2();
                }
            } catch (PublishedException2 e2) {
            } catch (UnpublishedException2 e2) {
            } 
        } catch (UnpublishedException1 e) {
        } finally {
            try {
                if (test2) {
                    throw new PublishedException3();
                } else {
                    throw new UnpublishedException3();
                }
            } catch (PublishedException3 e3) {
            } catch (UnpublishedException3 e3) {
            }
        }
    }

    // 内部クラス中
    public static class InnerClass {
        public void testInnerClassException(boolean test1) throws PublishedException1, UnpublishedException1 {
        }

        public void testExceptionCatcher(boolean test2) {

            try {
                testInnerClassException(true);
            } catch (PublishedException1 e) {
            } catch (UnpublishedException1 e) {
            }
        }
    }

    public void testLocalClassException() {

        // ローカルクラス中
        class LocalClass {
            public void testLocalClassException(boolean test1) throws PublishedException1, UnpublishedException1 {
            }

            public void testExceptionCatcher(boolean test2) {

                try {
                    testLocalClassException(true);
                } catch (PublishedException1 e) {
                } catch (UnpublishedException1 e) {
                }
            }
        }

        LocalClass localClass = new LocalClass();
        localClass.testExceptionCatcher(false);
    }

    public void testForAnnonymousClassException() {

        // 匿名クラス中
        InterfazeForExceptionCatcher anno = new InterfazeForExceptionCatcher() {

            public void testLocalClassException(boolean test1) throws PublishedException1, UnpublishedException1 {
            }

            public void testExceptionCatcher(boolean test2) {

                try {
                    testLocalClassException(true);
                } catch (PublishedException1 e) {
                } catch (UnpublishedException1 e) {
                }
            }
        };

        // 不要な指摘回避
        System.out.println(anno);
    }
}
