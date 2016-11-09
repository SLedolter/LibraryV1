import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jesta_000 on 29.09.2016.
 */

public class DownloadIntentService extends IntentService {

  public static final String TEXT_INPUT = "inText";
  public static final String TEXT_OUTPUT = "outText";

  public DownloadIntentService() {
    super("DownloadIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // do the work

    // send broadcast on completion
  }
}
