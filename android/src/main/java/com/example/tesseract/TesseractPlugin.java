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
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.HashMap;


/** TesseractPlugin */
public class TesseractPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tesseract");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("extractText")) {
//      final int DEFAULT_PAGE_SEG_MODE = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK;
      final String tessDataPath = call.argument("tessData");
      final String imagePath = call.argument("imagePath");
      String DEFAULT_LANGUAGE = "eng";

      if (call.argument("language") != null) {
        DEFAULT_LANGUAGE = call.argument("language");
      }

//      ArrayList<Rect> rectArrayList=new ArrayList<Rect>();
      ArrayList<ArrayList<Integer>> boundingRectList=new ArrayList<ArrayList<Integer>>();
      ArrayList<String> ocrTextElement = new ArrayList<String>();

      final String recognizedText;
      final File tempFile = new File(imagePath);
      final TessBaseAPI baseApi = new TessBaseAPI();
      baseApi.setDebug(false);
      baseApi.init(tessDataPath, DEFAULT_LANGUAGE);
      baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
      baseApi.setImage(tempFile);
      baseApi.getHOCRText(0);

//      recognizedText = baseApi.getUTF8Text();


      ResultIterator iterator = baseApi.getResultIterator();
      iterator.begin();
      do {
        Rect rect=iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
        ArrayList<Integer> boundingRect=new ArrayList<Integer>();
        boundingRect.add(rect.left);
        boundingRect.add(rect.top);
        boundingRect.add(rect.right);
        boundingRect.add(rect.bottom);

        boundingRectList.add(boundingRect);

//        rectArrayList.add(iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        ocrTextElement.add(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD));
      } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));
      iterator.delete();
//      recognizedText = baseApi.getUTF8Text();
      recognizedText = baseApi.getUTF8Text();
      baseApi.stop();
      HashMap<String,Object> dataMap = new HashMap<String,Object>();
      if(ocrTextElement!=null&&boundingRectList!=null&&ocrTextElement.size()==boundingRectList.size()){

        for (int i = 0; i < ocrTextElement.size(); i++) {
          dataMap.put("TEXT",recognizedText);
          dataMap.put("TEXT_ELEMENT",ocrTextElement);
          dataMap.put("TEXT_ELEMENT_RECT",boundingRectList);
        }
      }
      result.success(dataMap);

    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
