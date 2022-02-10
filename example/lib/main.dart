import 'dart:io';
import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui';
import 'package:flutter/services.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:tesseract/tesseract.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _scanning = false;
  String _extractText = '';
  int _scanTime = 0;

  static const String TESS_DATA_CONFIG = 'assets/tessdata_config.json';
  static const String TESS_DATA_PATH = 'assets/tessdata';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Tesseract OCR'),
          ),
          body: Container(
            padding: EdgeInsets.all(16),
            child: ListView(
              children: <Widget>[
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Expanded(
                      child: TextButton(
                          onPressed: () {
                            print("tap");
                          },
                          child: Text("Click")),
                    ),
                    RaisedButton(
                      child: Text('Select image'),
                      onPressed: () async {
                        FilePickerResult? result = await FilePicker.platform.pickFiles(type: FileType.image);
                        if (result != null) {
                          File file = File(result.files.single.path!);
                          String tessData = await _loadTessData();
                          _scanning = true;

                          setState(() {});

                          var watch = Stopwatch()..start();
                          TesseractResultModel tesseractResultModel;
                          tesseractResultModel = await Tesseract.extractText(file.path, tessData, language: "hin");
                          print("main dart ${tesseractResultModel.rectList}");
                          print(tesseractResultModel.rectList.runtimeType);
                          _extractText = tesseractResultModel.ocrText;
                          _scanTime = watch.elapsedMilliseconds;

                          _scanning = false;
                          setState(() {});
                        } else {
                          // User canceled the picker
                        }
                      },
                    ),
                    _scanning
                        ? SpinKitCircle(
                            color: Colors.black,
                          )
                        : Icon(Icons.done),
                  ],
                ),
                SizedBox(
                  height: 16,
                ),
                Text(
                  'Scanning took $_scanTime ms',
                  style: TextStyle(color: Colors.red),
                ),
                SizedBox(
                  height: 16,
                ),
                Center(child: SelectableText(_extractText)),
              ],
            ),
          )),
    );
  }

  static Future<String> _loadTessData() async {
    final Directory appDirectory = await getApplicationDocumentsDirectory();
    final String tessdataDirectory = join(appDirectory.path, 'tessdata');

    if (!await Directory(tessdataDirectory).exists()) {
      await Directory(tessdataDirectory).create();
    }
    await _copyTessDataToAppDocumentsDirectory(tessdataDirectory);
    return appDirectory.path;
  }

  static Future _copyTessDataToAppDocumentsDirectory(String tessdataDirectory) async {
    final String config = await rootBundle.loadString(TESS_DATA_CONFIG);
    Map<String, dynamic> files = jsonDecode(config);
    for (var file in files["files"]) {
      if (!await File('$tessdataDirectory/$file').exists()) {
        final ByteData data = await rootBundle.load('$TESS_DATA_PATH/$file');
        final Uint8List bytes = data.buffer.asUint8List(
          data.offsetInBytes,
          data.lengthInBytes,
        );
        await File('$tessdataDirectory/$file').writeAsBytes(bytes);
      }
    }
  }
}
