import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  //FlutterBlue flutterBlue = FlutterBlue.instance;

  //List<BluetoothDevice> devicesList = [];

  @override
  void initState() {
    super.initState();
    //initBluetooth();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    const sendChannel = MethodChannel("samples.flutter.dev/blueprint");
    const  receiveChannel = "samples.flutter.dev/blueprint_send";

    return SingleChildScrollView(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          ElevatedButton(
            onPressed: () async {
              final result = await sendChannel.invokeMethod('connect');
              print(result);
            },
            child: const Text('connect'),
          ),
          ElevatedButton(
            onPressed: () async {
              final result = await sendChannel.invokeMethod('print');
              print(result);
            },
            child: const Text('print'),
          ),
        ],
      ),
    );
  }

  // Future<void> initBluetooth() async {
  //   flutterBlue.state.listen((state) {
  //     if (state == BluetoothState.on) {
  //       scanDevices();
  //     }
  //   });
  // }
  //
  // void scanDevices() {
  //   flutterBlue.connectedDevices.then((devices) {
  //     for (BluetoothDevice device in devices) {
  //       devicesList.add(device);
  //     }
  //   });
  // }
}

