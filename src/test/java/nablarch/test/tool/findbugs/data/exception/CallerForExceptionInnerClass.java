package nablarch.test.tool.findbugs.data.exception;

import nablarch.test.tool.findbugs.data.exception.PublishedException4.InternalPublishedException;
import nablarch.test.tool.findbugs.data.exception.UnPublishedException4.InternalUnPublishedException;

public class CallerForExceptionInnerClass {

    // 使用が許可された内部クラス（例外クラス）がthrows句に定義された場合に
    // findbugsがエラーとして検知しないことを確認するためのメソッド定義
    public void methodForCheckingAllowedInnerClassAtThrows() throws InternalPublishedException {
    }

    // 使用が許可された内部クラス（例外クラス）がcatch句に定義された場合に
    // findbugsがエラーとして検知しないことを確認するためのメソッド定義
    public void methodForCheckingAllowedInnerClassAtCatch() {
        try{
            throw new InternalPublishedException();
        } catch (InternalPublishedException e) {
        }
    }

    // 使用が許可されていない内部クラス（例外クラス）がthrows句に定義された場合に
    // findbugsがエラーとして検知することを確認するためのメソッド定義
    public void methodForCheckingNotAllowedInnerClassAtThrows() throws InternalUnPublishedException {
    }

    // 使用が許可されていない内部クラス（例外クラス）がcatch句に定義された場合に
    // findbugsがエラーとして検知することを確認するためのメソッド定義
    public void methodForCheckingNotAllowedInnerClassAtCatch() {
        try{
            throw new InternalUnPublishedException();
        } catch (InternalUnPublishedException e) {
        }
    }

}
