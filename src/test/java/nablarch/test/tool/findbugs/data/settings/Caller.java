package nablarch.test.tool.findbugs.data.settings;

import nablarch.test.tool.findbugs.data.setting.method.clazz.PublishedClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.TestClassForInnerClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.UnpublishedClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.abztract.PublishedAbstractClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.abztract.SubClassOfPublishedAbstractClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.abztract.SubClassOfUnpublishedAbstractClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.abztract.UnpublishedAbstractClass;
import nablarch.test.tool.findbugs.data.setting.method.clazz.interfaze.PublishedInterface;
import nablarch.test.tool.findbugs.data.setting.method.clazz.interfaze.PublishedInterfaceImpl;
import nablarch.test.tool.findbugs.data.setting.method.clazz.interfaze.UnpublishedInterface;
import nablarch.test.tool.findbugs.data.setting.method.clazz.interfaze.UnpublishedInterfaceImpl;
import nablarch.test.tool.findbugs.data.setting.method.method.or.constructor.MethodOrConstructor;
import nablarch.test.tool.findbugs.data.setting.method.publishedpackage.PublishedPackage;
import nablarch.test.tool.findbugs.data.setting.method.publishedpackage.PublishedPackage2;
import nablarch.test.tool.findbugs.data.setting.method.publishedpackage.sub.SubPackage;
import nablarch.test.tool.findbugs.data.setting.method.unpublishedpackage.UnpublishedPackage;
import nablarch.test.tool.findbugs.data.setting.method.unpublishedpackage.sub.SubUnpublishedPackage;

/**
 * 設定方法別指摘確認
 * 
 * @author 香川朋和
 */
public class Caller {

    public static void testForMethodOrConstructorSetting() {
        // メソッド・コンストラクタ指定公開
        // コンストラクタ
        MethodOrConstructor methodOrConstructorTest = new MethodOrConstructor();
        MethodOrConstructor methodOrConstructorTest2 = new MethodOrConstructor("Unpublished"); // オーバーロード
        // メソッド
        methodOrConstructorTest.publicMethod();
        methodOrConstructorTest.publicMethod("Published"); // オーバーロード
        methodOrConstructorTest.unpubliceMethod();
        methodOrConstructorTest.unpubliceMethod("Unpublished"); // オーバーロード

        // 不要な指摘回避
        System.out.println(methodOrConstructorTest2);
    }

    public static void testForPackageSettings() {
        // パッケージ指定
        // 公開パッケージ
        PublishedPackage publishedPackage = new PublishedPackage();
        publishedPackage.publishedPackageTest();
        publishedPackage.publishedPackageTest2();
        PublishedPackage2 publishedPackage2 = new PublishedPackage2();
        publishedPackage2.publishedPackageTest();
        // サブパッケージも公開される。
        SubPackage subPackage = new SubPackage();
        subPackage.published();
        // 非公開パッケージ
        UnpublishedPackage unpublishedPackage = new UnpublishedPackage();
        unpublishedPackage.unpublishedPackageTest();
        unpublishedPackage.unpublishedPackagaTest2();
        // サブパッケージも非公開
        SubUnpublishedPackage subUnpublishedPackage = new SubUnpublishedPackage();
        subUnpublishedPackage.unpublished();
    }

    public static void testForClassSetting() {
        // クラス指定
        PublishedClass publishedClass = new PublishedClass();
        publishedClass.publishedClassTest();
        publishedClass.publishedClassTest2();
        UnpublishedClass unpublishedClass = new UnpublishedClass();
        unpublishedClass.unpublishedClassTest();
        // 内部クラス
        TestClassForInnerClass.InnerClassPublished innerClassPublished = new TestClassForInnerClass.InnerClassPublished();
        innerClassPublished.innerClassPublished();
        TestClassForInnerClass.InnerClassUnpublished innerClassUnpublished = new TestClassForInnerClass.InnerClassUnpublished();
        innerClassUnpublished.innerClassUnpublished();
        // 抽象クラス
        PublishedAbstractClass publishedAbstractClass = new SubClassOfPublishedAbstractClass();
        publishedAbstractClass.publishedAbstract();
        UnpublishedAbstractClass unpublishedAbstractClass = new SubClassOfUnpublishedAbstractClass();
        unpublishedAbstractClass.unpublishedAbstract();
        // インタフェース
        PublishedInterface publishedInterface = new PublishedInterfaceImpl();
        publishedInterface.publishedInterface();
        UnpublishedInterface unpublishedInterface = new UnpublishedInterfaceImpl();
        unpublishedInterface.unpublishedInterface();
    }
}
