package nablarch.test.tool.findbugs.data.methodcall;

import nablarch.test.tool.findbugs.data.methodcall.inherit.method.ClassB;
import nablarch.test.tool.findbugs.data.methodcall.methods.PublishedInterfaceSettings;
import nablarch.test.tool.findbugs.data.methodcall.methods.PublishedMethods;
import nablarch.test.tool.findbugs.data.methodcall.methods.UnpublishedMethods;

public class ClassForVariousLocation {

    static private PublishedMethods publishedMethodsInstance = new PublishedMethods();
    static private UnpublishedMethods unpublishedMethodsInstance = new UnpublishedMethods();

    static {
        // 静的初期化子中メソッドコール
        publishedMethodsInstance.publishedMethodBooleanReturn();
        unpublishedMethodsInstance.unpublishedMethodBooleanReturn();
    }

    {
        // インスタンス初期化子中メソッドコール
        publishedMethodsInstance.publishedMethodBooleanReturn();
        unpublishedMethodsInstance.unpublishedMethodBooleanReturn();
    }

    public ClassForVariousLocation() { // コンストラクタ中
        publishedMethodsInstance.publishedMethodBooleanReturn();
        unpublishedMethodsInstance.unpublishedMethodBooleanReturn();
    }

    public PublishedInterfaceSettings annonymous = new PublishedInterfaceSettings() { // 匿名クラス中
        private PublishedMethods publishedMethods = new PublishedMethods();
        private UnpublishedMethods unpublishedMethods = new UnpublishedMethods();

        // 匿名クラス中メソッドコール
        public void publishedInterface() {
            publishedMethods.publishedMethodBooleanReturn();
            unpublishedMethods.unpublishedMethodBooleanReturn();
        }
    };

    public void testForLocalClass() { // ローカルクラス中

        class Local { // 継承なしローカルクラス中
            private PublishedMethods publishedMethods = new PublishedMethods();
            private UnpublishedMethods unpublishedMethods = new UnpublishedMethods();

            // ローカルクラス中メソッドコール
            public void testLocal() {
                publishedMethods.publishedMethodBooleanReturn();
                unpublishedMethods.unpublishedMethodBooleanReturn();
            }
        }

        class LocalInheritance extends ClassB { // 継承ありローカルクラス中
            public void methodD() {
                // オーバーライドあり
                publishedPublishedOverriddenMethod();
                publishedUnpublishedOverriddenMethod();
                unpublishedPublishedOverriddenMethod();
                unpublishedUnpublishedOverriddenMethod();

                // オーバーライドなし
                publishedMethodA();
                unpublishedMethodA();
            }
        }

        Local local = new Local();
        local.testLocal();

        LocalInheritance localInheritance = new LocalInheritance(); // ローカルクラスの生成はチェックしない。
        // オーバーライドあり
        // 親クラス公開
        localInheritance.publishedPublishedOverriddenMethod();
        localInheritance.publishedUnpublishedOverriddenMethod();
        // 子クラス公開
        localInheritance.unpublishedPublishedOverriddenMethod();
        localInheritance.unpublishedUnpublishedOverriddenMethod();
        // オーバーライドなし
        localInheritance.publishedMethodA();
        localInheritance.unpublishedMethodA();

        localInheritance.methodD(); // ローカルクラスに定義されたオーバーライドしていないメソッドへのチェックは行わない。

    }

    static class Inner { // 内部クラス中

        private PublishedMethods publishedMethods = new PublishedMethods();
        private UnpublishedMethods unpublishedMethods = new UnpublishedMethods();

        // 内部クラス中メソッドコール
        public void testForInnerClass() {
            publishedMethods.publishedMethodBooleanReturn();
            unpublishedMethods.unpublishedMethodBooleanReturn();
        }
    }
}
