package nablarch.test.tool.findbugs.data.methodcall;

import nablarch.test.tool.findbugs.data.methodcall.chain.MethodChain;
import nablarch.test.tool.findbugs.data.methodcall.inherit.clazz.SubOfPublishedClassA;
import nablarch.test.tool.findbugs.data.methodcall.inherit.clazz.SubOfUnpublishedClassA;
import nablarch.test.tool.findbugs.data.methodcall.inherit.method.ClassC;
import nablarch.test.tool.findbugs.data.methodcall.inherit.pack.SubOfPublishedPackClassA;
import nablarch.test.tool.findbugs.data.methodcall.inherit.pack.SubOfUnpublishedPackClassA;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.clazz.PublishedInterfaze;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.clazz.PublishedInterfazeImple;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.clazz.UnpublishedInterfaze;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.clazz.UnpublishedInterfazeImple;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.method.InterfazeMethod;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.method.InterfazeMethodImpl;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.pack.PublishedPackInterfazeImpl;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.pack.UnpublishedPackInterfazeImpl;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.published.pack.PublishedPackInterfaze;
import nablarch.test.tool.findbugs.data.methodcall.interfaze.unpublished.pack.UnpublishedPackInterfaze;
import nablarch.test.tool.findbugs.data.methodcall.methods.PublishedException;
import nablarch.test.tool.findbugs.data.methodcall.methods.PublishedMethods;
import nablarch.test.tool.findbugs.data.methodcall.methods.UnpublishedException;
import nablarch.test.tool.findbugs.data.methodcall.methods.UnpublishedMethods;
import nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterface;
import nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.SubInterfaceImple;

public class Caller {

    private PublishedMethods publishedMethods = new PublishedMethods();
    private UnpublishedMethods unpublishedMethods = new UnpublishedMethods();

    public void testParam() {
        String[] strs = { "aaa", "bbb" };
        strs.clone();
        publishedMethods.publishedMethodVariableParams("published", "published2");
        unpublishedMethods.unpublishedMethodVariableParams("unpublished", "unpublished2");
    }

    public void testChain() { // メソッドチェインに対する指摘検証
        MethodChain methodChain = new MethodChain();
        methodChain.testPublishedChain().testPublishedChain();
        methodChain.testUnublishedChain().testUnublishedChain();
    }

    public void testIf() { // if文中での指摘検証

        if (publishedMethods.publishedMethodBooleanReturn()) {
            publishedMethods.publishedMethodBooleanReturn();
            if (publishedMethods.publishedMethodBooleanReturn()) { // ネスト中
                publishedMethods.publishedMethodBooleanReturn(); // ネスト中
            }
        }

        if (unpublishedMethods.unpublishedMethodBooleanReturn()) {
            unpublishedMethods.unpublishedMethodBooleanReturn();
            if (unpublishedMethods.unpublishedMethodBooleanReturn()) { // ネスト中
                unpublishedMethods.unpublishedMethodBooleanReturn(); // ネスト中
            }
        }
    }

    public void testFor() { // for文中での指摘検証
        for (int i = 0; publishedMethods.publishedMethodBooleanReturn(); i++) {
            publishedMethods.publishedMethodBooleanReturn();
        }

        for (int i = 0; unpublishedMethods.unpublishedMethodBooleanReturn(); i++) {
            unpublishedMethods.unpublishedMethodBooleanReturn();
        }
    }

    public void testWhile() { // while文中での指摘検証
        while (publishedMethods.publishedMethodBooleanReturn()) {
            publishedMethods.publishedMethodBooleanReturn();
        }

        while (unpublishedMethods.unpublishedMethodBooleanReturn()) {
            unpublishedMethods.unpublishedMethodBooleanReturn();
        }
    }

    public void testDoWhile() { // do-while文中での指摘検証
        do {
            publishedMethods.publishedMethodBooleanReturn();
        } while (publishedMethods.publishedMethodBooleanReturn());

        do {
            unpublishedMethods.unpublishedMethodBooleanReturn();
        } while (unpublishedMethods.unpublishedMethodBooleanReturn());
    }

    public void testSwitch() { // switch文中での指摘検証

        switch (publishedMethods.publishedMethodIntReturn()) {
        case 2:
            publishedMethods.publishedMethodIntReturn();
            break;

        default:
            break;
        }

        switch (unpublishedMethods.unpublishedMethodIntReturn()) {
        case 1:
            unpublishedMethods.unpublishedMethodIntReturn();
            break;

        default:
            break;
        }
    }

    public void testTernaryOperator() { // 三項演算子中で指摘検証
        String test = publishedMethods.publishedMethodBooleanReturn() ? "aaa" : "bbbb";
        String test2 = unpublishedMethods.unpublishedMethodBooleanReturn() ? "aaaa" : "bbb";

        // 不要な指摘を回避
        System.out.println(test);
        System.out.println(test2);
    }

    public int testReturn() { // return文中での指摘検証
        if (publishedMethods.publishedMethodBooleanReturn()) {
            return publishedMethods.publishedMethodIntReturn();
        } else {
            return unpublishedMethods.unpublishedMethodIntReturn();
        }
    }

    public void testCatchAndFinally() { // catch文、finally文中での指摘検証
        try {
            if (publishedMethods.publishedMethodBooleanReturn()) {
                throw new PublishedException();
            } else {
                throw new UnpublishedException();
            }
        } catch (PublishedException e) {

        } catch (UnpublishedException e) {

        } finally {
            publishedMethods.publishedMethodBooleanReturn();
            unpublishedMethods.unpublishedMethodBooleanReturn();
        }
    }

    public void testInheritance() { // 継承に関する指摘検証

        // メソッド指定公開
        ClassC classC = new ClassC();
        // オーバーライドあり
        // 親クラス公開
        classC.publishedPublishedOverriddenMethod();
        classC.publishedUnpublishedOverriddenMethod();
        // 子クラス公開
        classC.unpublishedPublishedOverriddenMethod();
        classC.unpublishedUnpublishedOverriddenMethod();
        // オーバーライドなし
        classC.publishedMethodA();
        classC.unpublishedMethodA();

        // クラス指定公開
        SubOfUnpublishedClassA subOfUnpublishedClassA = new SubOfUnpublishedClassA();
        subOfUnpublishedClassA.methodA();
        SubOfPublishedClassA subOfPublishedClassA = new SubOfPublishedClassA();
        subOfPublishedClassA.methodA();

        // パッケージ指定
        SubOfUnpublishedPackClassA subOfUnpublishedPackClassA = new SubOfUnpublishedPackClassA();
        subOfUnpublishedPackClassA.methodA();
        SubOfPublishedPackClassA subOfPublishedPackClassA = new SubOfPublishedPackClassA();
        subOfPublishedPackClassA.methodA();

    }

    public void testInterface() {
        // メソッド指定
        InterfazeMethod interfazeMethod = new InterfazeMethodImpl();
        interfazeMethod.publishedInterfaceMethod();
        interfazeMethod.unpublishedInterfaceMethod();

        // インタフェース指定
        PublishedInterfaze publishedInterfaze = new PublishedInterfazeImple();
        publishedInterfaze.method();
        UnpublishedInterfaze unpublishedInterfazeImple = new UnpublishedInterfazeImple();
        unpublishedInterfazeImple.method();

        // パッケージ指定
        PublishedPackInterfaze publishedPackInterfaze = new PublishedPackInterfazeImpl();
        publishedPackInterfaze.methodA();
        UnpublishedPackInterfaze unpublishedPackInterfaze = new UnpublishedPackInterfazeImpl();
        unpublishedPackInterfaze.methodA();

        // 無名クラス
        InterfazeMethod annonyouse = new InterfazeMethod() {

            public void unpublishedInterfaceMethod() {
            }

            public void publishedInterfaceMethod() {
            }
        };
        annonyouse.publishedInterfaceMethod();
        annonyouse.unpublishedInterfaceMethod();

        SubInterface subInterface = new SubInterfaceImple();
        subInterface.subPublishedInterfaceMethod();
        subInterface.subUnpublishedInterfaceMethod();
        subInterface.superPublishedInterfaceMethod();
        subInterface.superUnpublishedMethod();

    }
}
