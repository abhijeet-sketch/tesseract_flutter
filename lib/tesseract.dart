import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:flutter/services.dart';

//
class Tesseract {
  static const MethodChannel _channel = const MethodChannel('tesseract');

  static Future<TesseractResultModel> extractText(String imagePath, String tessDataParentDirectoryPath, {String language}) async {
    assert(await File(imagePath).exists(), true);
    final String tessData = tessDataParentDirectoryPath;
    List<String> textElementList;
    List<List<double>> point;

    var result = await _channel.invokeMethod('extractText', <String, dynamic>{
      'imagePath': imagePath,
      'tessData': tessData,
      'language': language,
    });
    Map<String, dynamic> data = new Map<String, dynamic>.from(result);
    List<Rect> rectList = [];
    TesseractResultModel ocrResultModel;

    if (data != null) {
      textElementList = List<dynamic>.unmodifiable(data["TEXT_ELEMENT"].map<dynamic>((text) => text)).cast<String>();
      point = List<List<double>>.unmodifiable(data["TEXT_ELEMENT_RECT"].map<List<double>>((e) => [e[0], e[1], e[2], e[3]].cast<double>()));
      for (int i = 0; i < point.length; i++) {
        rectList.add(new Rect.fromLTRB(point[i][0], point[i][1], point[i][2], point[i][3]));
      }

      ocrResultModel = TesseractResultModel(ocrText: data["TEXT"], textElementList: textElementList, rectList: rectList);
    }
    print(rectList[0].runtimeType);
    print(rectList[0].top);
    print(textElementList.length);
    return ocrResultModel;
  }
}

class TesseractResultModel {
  List<Rect> rectList;
  List<String> textElementList;
  String ocrText = "";

  TesseractResultModel({
    this.textElementList,
    this.rectList,
    this.ocrText,
  });
}
