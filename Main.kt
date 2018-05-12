import arithmeticEncoder.ArithmeticDecoder
import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.BitInputStream
import arithmeticEncoder.BitOutputStream
import java.io.*


fun main(args: Array<String>) {
    val inputFile = File("test.txt")
    val outputFile = File("test.txt.PPM")

    val operation = 'd'
    val maxContextOrder = 2

    when(operation) {
        'c' -> {
            BufferedInputStream(FileInputStream(inputFile)).use { input ->
                BitOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { out ->
                    compress(input, out, maxContextOrder)
                }
            }
        }

        'd' -> {
            BitInputStream(FileInputStream(inputFile)).use { input ->
                BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
                    decompress(input, out, maxContextOrder)
                }
            }
        }
    }

}

fun compress(input: InputStream, output: BitOutputStream, maxContextOrder: Int) {

    val encoder = ArithmeticEncoder(output)
    val ppmEncoder = PpmEncoder(encoder, maxContextOrder)

    while(true) {
        val symbol = input.read()

        if(symbol == -1) break // EOF

        ppmEncoder.encode(symbol)
    }

    ppmEncoder.encode(256) // EOF

    encoder.finish()
}

fun decompress(input: BitInputStream, output: OutputStream, maxContextOrder: Int) {

    val decoder = ArithmeticDecoder(input)
    val ppmDecoder = PpmDecoder(decoder, maxContextOrder)

    while(true) {
        val symbol = ppmDecoder.decode()
        if(symbol == 256) return

        output.write(symbol)
    }

}

