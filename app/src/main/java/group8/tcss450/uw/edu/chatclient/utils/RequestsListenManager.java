package group8.tcss450.uw.edu.chatclient.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Thread manager that periodically checks the web server for updates. Building the manager
 * requires the fully formed URL and Consumer that processes the results.
 *
 * When the Consumer processing the results needs to manipulate any UI elements, this must be
 * performed on the UI Thread. See the following:
 *
 * <pre>
 *      runOnUiThread(() -> {
 *          // statements that manipulate UI Views
 *          // do not include non-UI related statements.
 *      }
 * </pre>
 *
 *  Do not include statements that do not need to manipulate UI Views inside of the runOnUiThread
 *  argument.
 *
 * @author Charles Bryan
 * @author Lloyd Brooks
 * @version 4/14/2018
 */
public class RequestsListenManager {

    private final String mURL;
    private final Consumer<JSONObject> mActionToTake;
    private final Consumer<Exception> mActionToTakeOnError;
    private final int mDelay;

    private ScheduledThreadPoolExecutor mPool;
    private ScheduledFuture mThread;

    /**
     * Helper class for building ListenManagers.
     *
     * @author Charles Bryan
     */
    public static class Builder {

        //Required Parameters
        private final String mURL;
        private final Consumer<JSONObject> mActionToTake;

        //Optional Parameters
        private int mSleepTime = 500;
        private Consumer<Exception> mActionToTakeOnError = e -> {};

        /**
         * Constructs a new Builder with a delay of 500 ms.
         *
         * When the Consumer processing the results needs to manipulate any UI elements, this must be
         * performed on the UI Thread. See ListenManager class documentation for more information.
         *
         * @param url the fully-formed url of the web service this task will connect to
         * @param actionToTake the Consumer processing the results
         */
        public Builder(String url, Consumer<JSONObject> actionToTake) {
            mURL = url;
            mActionToTake = actionToTake;
        }

        /**
         * Set the delay amount between calls to the web service. The default delay is 500 ms.
         * @param val the delay amount between calls to the web service
         * @return
         */
        public Builder setDelay(final int val) {
            mSleepTime = val;
            return this;
        }

        /**
         * Set the action to perform during exceptional handling. Note, not ALL possible
         * exceptions are handled by this consumer.
         *
         * @param val the action to perform during exceptional handling
         * @return
         */
        public Builder setExceptionHandler(final Consumer<Exception> val) {
            mActionToTakeOnError = val;
            return this;
        }

        /**
         * Constructs a ListenManager with the current attributes.
         *
         * @return a ListenManager with the current attributes.
         */
        public RequestsListenManager build() {
            return new RequestsListenManager(this);
        }

    }

    /**
     * Construct a ListenManager internally from a builder.
     *
     * @param builder the builder used to construct this object
     */
    private RequestsListenManager(final Builder builder) {
        mURL = builder.mURL;
        mActionToTake = builder.mActionToTake;
        mDelay = builder.mSleepTime;
        mActionToTakeOnError = builder.mActionToTakeOnError;
        mPool = new ScheduledThreadPoolExecutor(5);
    }

    /**
     * Starts the worker thread to ask for updates every delay milliseconds.
     */
    public void startListening() {
        mThread = mPool.scheduleAtFixedRate(new ListenForRequests(),
                0,
                mDelay,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stops listening for new messages.
     */
    public void stopListening() {
        mThread.cancel(true);
    }

    /**
     * Does the work!
     */
    private class ListenForRequests implements Runnable {

        @Override
        public void run() {
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;

            //go out and ask for new messages
            response = new StringBuilder();
            try {
                String getURL = mURL;

                URL urlObject = new URL(getURL);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response.append(s);
                }

                JSONObject messages = new JSONObject(response.toString());

                //here is where we "publish" the message that we received.
                mActionToTake.accept(messages);

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
                mActionToTakeOnError.accept(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }
}
