

import play.GlobalSettings;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

public class AICAGlobal extends GlobalSettings {
  private class ActionWrapper extends Action.Simple {
    public ActionWrapper(Action<?> action) {
        this.delegate = action;
    }

    @Override
    public Promise<SimpleResult> call(Http.Context ctx) throws java.lang.Throwable {
        Promise<SimpleResult> result = this.delegate.call(ctx);
        Http.Response response = ctx.response();
        response.setHeader("Access-Control-Allow-Origin", "*");
        return result;
    }
  }
  @Override
  public Action<?> onRequest(Http.Request request, java.lang.reflect.Method actionMethod) {
      return new ActionWrapper(super.onRequest(request, actionMethod));
  }
}
