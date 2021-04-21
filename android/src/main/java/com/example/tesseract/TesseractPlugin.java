package com.example.tesseract;

import androidx.annotation.NonNull;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.ResultIterator;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import java.io.File;
import android.os.Handler;
import android.os.Looper;
import android.os.AsyncTask;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** TesseractPlugin */
public class TesseractPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  /** Plugin registration. */
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tesseract");
    channel.setMethodCallHandler(this);
  }

  // MethodChannel.Result wrapper that responds on the platform thread.
  private static class MethodResultWrapper implements Result {
    private Result methodResult;
    private Handler handler;

    MethodResultWrapper(Result result) {
      methodResult = result;
      handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          methodResult.success(result);
        }
      });
    }

    @Override
    public void error(final String errorCode, final String errorMessage, final Object errorDetails) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          methodResult.error(errorCode, errorMessage, errorDetails);
        }
      });
    }

    @Override
    public void notImplemented() {
      handler.post(new Runnable() {
        @Override
        public void run() {
          methodResult.notImplemented();
        }
      });
    }
  }
  @Override
  public void onMethodCall(MethodCall call, Result rawResult) {

    Result result = new MethodResultWrapper(rawResult);

    if (call.method.equals("extractText")) {
      final String tessDataPath = call.argument("tessData");
      final String imagePath = call.argument("imagePath");
      String DEFAULT_LANGUAGE = "eng";
      if (call.argument("language") != null) {
        DEFAULT_LANGUAGE = call.argument("language");
      }
      calculateResult(tessDataPath, imagePath, DEFAULT_LANGUAGE, result);
    } else {
      result.notImplemented();
    }
  }





  private void calculateResult(final String tessDataPath, final String imagePath, final String language,
                               final Result result) {

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        final String recognizedText;
        List<double[]> rectList=new ArrayList<>();
        List<String> textElementList = new ArrayList<>();

        final File tempFile = new File(imagePath);
        final TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(false);
        baseApi.init(tessDataPath, language);
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
        baseApi.setImage(tempFile);
        baseApi.getHOCRText(0);


        ResultIterator iterator = baseApi.getResultIterator();
        iterator.begin();
        do {
          Rect rect=iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
          rectList.add(new double[]{(double) rect.left,(double) rect.top,(double) rect.right,(double) rect.bottom});
          textElementList.add(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        iterator.delete();
        recognizedText = baseApi.getUTF8Text();
        baseApi.stop();
        baseApi.end();
        Map<String, Object> dataMap = new HashMap<>();
        if(textElementList!=null&&rectList!=null&&textElementList.size()==rectList.size()){

          dataMap.put("TEXT",recognizedText);
          dataMap.put("TEXT_ELEMENT",textElementList);
          dataMap.put("TEXT_ELEMENT_RECT",rectList);
        }
        result.success(dataMap);
        return null;
      }

      @Override
      protected void onPostExecute(Void result) {
        super.onPostExecute(result);
      }
    }.execute();
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
