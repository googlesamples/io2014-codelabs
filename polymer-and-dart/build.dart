import 'package:polymer/builder.dart';

main(args) {
  build(entryPoints: ['web/index.html'],
        options: parseOptions(args));
}
