package si.dime.kotlin.tutorials.rest.booklibrary


import java.io.*
import java.util.*
import java.awt.image.*
import javax.imageio.*
import java.nio.file.*
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder
import com.beust.klaxon.*
import com.google.maps.*
import com.google.maps.model.PlaceType
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.*
import org.springframework.web.bind.annotation.*

@SpringBootApplication
open class App{

}

class dirreq{
    lateinit var origen: String
    lateinit  var destino: String
}

class restaureq{
    lateinit var origen: String
}

class imgsize{
    var width: Int = 0
    var height: Int = 0
}

class imageResizeReq {
    lateinit var nombre: String
    lateinit var data: String
    lateinit var tam: imgsize
}


class imagereq {
    lateinit var nombre: String
    lateinit var data: String
}


@RestController
class DirectionsClass{
    @RequestMapping(value = "/ejercicio1", method = arrayOf(RequestMethod.POST))
    fun getDirection(@RequestBody dirreq: dirreq) : Any? {
        val origin = dirreq.origen
        val destination = dirreq.destino
        val apikey = "AIzaSyDun5QvAmgTrK8_mJ6P542DDuTT2v4-N3g"

        val context = GeoApiContext().setApiKey(apikey);
        val result = DirectionsApi.getDirections(context,origin,destination).await()

        var jsonResult  = "{\n \"ruta\":["
        var ind = 0

        while(ind < result.routes[0].legs[0].steps.size){
            val latitud = result.routes[0].legs[0].steps[ind].startLocation.lat
            val longitud = result.routes[0].legs[0].steps[ind].startLocation.lng

            jsonResult += "\n\t{\n\t\t\"lat\": ${latitud},\n\t\t\"lng\": ${longitud}\n"

            if(ind == result.routes[0].legs[0].steps.size){
                jsonResult+="\t}"
            }else{
                jsonResult+="\t},"
            }

            ind++
        }

        jsonResult = jsonResult.substring(0,jsonResult.length-1)
        jsonResult += "\n  ]\n}"

        return jsonResult
    }
}

@RestController
class RestaurantesClass{
    @RequestMapping(value = "/ejercicio2", method = arrayOf(RequestMethod.POST))
    fun getRestaurant(@RequestBody restaureq: restaureq) : Any? {
        val origin = restaureq.origen
        val apikey = "AIzaSyDun5QvAmgTrK8_mJ6P542DDuTT2v4-N3g"

        val context = GeoApiContext().setApiKey(apikey);
        val result = GeocodingApi.geocode(context,origin).await()
        val request = NearbySearchRequest(context)

        request.location(result[0].geometry.location)
        request.radius(40000)
        request.type(PlaceType.RESTAURANT)
        request.keyword("restaurant")

        val response = request.await()

        var jsonResult  = "{\n \"restaurantes\":["
        var ind = 0

        while(ind < response.results.size){
            val nombre = response.results[ind].name
            val latitud = response.results[ind].geometry.location.lat
            val longitud = response.results[ind].geometry.location.lng

            jsonResult += "\n\t{\n\t\t\"nombre\": ${nombre},\n\t\t\"lat\": ${latitud},\n\t\t\"lng\": ${longitud}\n"

            if(ind == response.results.size){
                jsonResult+="\t}"
            }else{
                jsonResult+="\t},"
            }

            ind++
        }

        jsonResult = jsonResult.substring(0,jsonResult.length-1)
        jsonResult += "\n  ]\n}"

        return jsonResult
    }
}

@RestController
class GrayClass{
    @RequestMapping(value = "/ejercicio3", method = arrayOf(RequestMethod.POST))
    fun grayscaleIMG(@RequestBody imagereq: imagereq) : Any? {
        val data = imagereq.data

        val image2: BufferedImage?
        val imageByte: ByteArray

        val decoder = BASE64Decoder()
        imageByte = decoder.decodeBuffer(data)
        val bis = ByteArrayInputStream(imageByte)
        image2 = ImageIO.read(bis)
        val width = image2.getWidth()
        val height = image2.getHeight()

        var x = 0
        while(x < width - 1){
            var y = 0
            while(y < height - 1){
                val rgb = image2.getRGB(x, y)

                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = (rgb and 0xFF)

                val grayLevel = ((r + g + b) / 3)
                val gray = grayLevel shl 16 or (grayLevel shl 8) or grayLevel

                image2.setRGB(x, y, gray)
                y++
            }
            x++
        }

        val outputfile = File("grayscaled_${imagereq.nombre}.bmp")
        ImageIO.write(image2, "bmp", outputfile)

        val finale = File("grayscaled_${imagereq.nombre}.bmp")
        val byte : ByteArray = Files.readAllBytes(finale.toPath())
        val encoder = Base64.getEncoder().encodeToString(byte)

        val jsonResult = "{\n\t\"nombre\": \"grayscaled_${imagereq.nombre}\",\n\t\"data\": \""+encoder+"\"\n}"

        return jsonResult
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(App::class.java, *args)
}