import arithmeticEncoder.ArithmeticDecoder
import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.BitInputStream
import arithmeticEncoder.BitOutputStream
import java.io.*
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {
    val time = measureTimeMillis {

        val op = args[0][1]
        when(op) {

            'c' -> {
                println("Comprimindo...")
                if (args.size != 3) throw IllegalArgumentException("Arguments: [-c k | -d] file")

                val maxContextOrder = args[1].toInt()
                val filename = args[2]
                compress(filename, maxContextOrder)
            }

            'd' -> {
                println("Descomprimindo...")
                if (args.size != 2) throw IllegalArgumentException("Arguments: [-c k | -d] file")

                val filename = args[1]
                decompress(filename)
            }

            else -> throw IllegalArgumentException("Invalid operation. Arguments: [-c k | -d] file")
        }

    }

    println("t: $time ms")
}

fun compress(filename: String, maxContextOrder: Int) {
    val input = BufferedInputStream(FileInputStream(File(filename)))
    val output = BitOutputStream(BufferedOutputStream(FileOutputStream(File("$filename.ppm"))))

    output.write8BitInt(maxContextOrder)

    val encoder = ArithmeticEncoder(output)
    val ppmEncoder = PpmEncoder(encoder, maxContextOrder)

    while(true) {
        val symbol = input.read()

        if(symbol == -1) break // EOF

        ppmEncoder.encode(symbol)
    }

    ppmEncoder.encode(256) // EOF

    encoder.finish()
    input.close()
    output.close()
}

fun decompress(filename: String) {
    if(!filename.endsWith(".ppm"))
        throw IllegalArgumentException("Not a .ppm file")

    val input = BitInputStream(FileInputStream(File(filename)))
    val output = BufferedOutputStream(FileOutputStream(filename.removeSuffix(".ppm") + '2'))

    val maxContextOrder = input.read8BitInt()

    val decoder = ArithmeticDecoder(input)
    val ppmDecoder = PpmDecoder(decoder, maxContextOrder)

    while(true) {
        val symbol = ppmDecoder.decode()
        if(symbol == 256) break

        output.write(symbol)
    }

    output.close()
    input.close()
}

fun BitOutputStream.write8BitInt(int: Int) {
    for(i in 0 until 8) {
        this.write((int shr (7-i)) and 1)
    }
}

fun BitInputStream.read8BitInt(): Int {
    var int = 0
    for(i in 0 until 8) {
        int = (int shl 1) or this.read()
    }
    return int
}