import android.content.Intent;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import java.lang.ClassNotFoundException;

public class ActivityStarterPlugin extends CordovaPlugin {

    private static final String LOG_TAG = "ActivityStarter";
    private static final String startActivityActionName = "startActivity";

    public ActivityStarterPlugin() {
}
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext)
    {
        String targetActivityName = "";
        Class<?> klass;
        Log.d(LOG_TAG, "Received Action: " + action);

        if (action.equals(startActivityActionName)){
            try {
                targetActivityName = args.getJSONObject(0).getString("targetActivityName");
                Log.d(LOG_TAG, "targetActivityName : " + targetActivityName);
                klass = Class.forName(targetActivityName);
            }
            catch(ClassNotFoundException e){
                Log.e(LOG_TAG, e.toString());
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION));
                return false;
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.toString());
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return false;
            }

            Intent intent = new Intent(this.cordova.getContext(), klass);
            this.cordova.getActivity().startActivity(intent);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            return true;
        }

        Log.e(LOG_TAG, "Invalid Action");
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
        return false;
    }
}
