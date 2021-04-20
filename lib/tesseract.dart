import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/services.dart';

//
class Tesseract {
  static const MethodChannel _channel = const MethodChannel('tesseract');

  static Future<TesseractResultModel> extractText(String imagePath, String tessDataParentDirectoryPath, {String language}) async {
    assert(await File(imagePath).exists(), true);
    final String tessData = tessDataParentDirectoryPath;

    var result = await _channel.invokeMethod('extractText', <String, dynamic>{
      'imagePath': imagePath,
      'tessData': tessData,
      'language': "hin",
    });

    List<Rect> rectList = [];
    TesseractResultModel ocrResultModel;
    if (result != null) {
      result["TEXT_ELEMENT_RECT"].forEach((e) {
        List<dynamic> coordinate = e;
        Rect rect = new Rect.fromLTRB(coordinate[0].toDouble(), coordinate[1].toDouble(), coordinate[2].toDouble(), coordinate[3].toDouble());
        rectList.add(rect);
      });

      ocrResultModel = TesseractResultModel(ocrText: result["TEXT"], textElementList: result["TEXT_ELEMENT"].cast<String>(), rectList: rectList);
    }

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
