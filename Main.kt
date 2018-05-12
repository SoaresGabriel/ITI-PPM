import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.BitOutputStream
import java.io.*


fun main(args: Array<String>) {
    val inputFile = File("generated.bin")
    val outputFile = File("generated.bin.PPM")

    BufferedInputStream(FileInputStream(inputFile)).use { input ->
        BitOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { out ->
            compress(input, out)
        }
    }
}

fun compress(input: InputStream, output: BitOutputStream) {
    val enconder = ArithmeticEncoder(output)
    val ppmEncoder = PpmEncoder(enconder, 5)
    while(true) {
        val symbol = input.read()

        if(symbol == -1) break // EOF

        ppmEncoder.encode(symbol)
    }

    enconder.finish()
}

