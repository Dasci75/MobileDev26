import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import java.io.FileInputStream
import com.google.firebase.FirebaseApp

fun main() {
    // Initialize Firebase Admin SDK
    // This assumes you have your service account key in 'app/google-services.json'
    // and that you run this script from the project root.
    val serviceAccount = FileInputStream("app/google-services.json")
    val credentials = GoogleCredentials.fromStream(serviceAccount)
    val firestoreOptions = FirestoreOptions.newBuilder().setCredentials(credentials).build()
    val db = firestoreOptions.service


    // Capitalize trip fields
    val trips = db.collection("trips").get().get()
    println("Starting trip capitalization...")
    for (trip in trips.documents) {
        val country = trip.getString("country")
        val city = trip.getString("cityId")
        val updates = mutableMapOf<String, Any?>()

        if (country != null) {
            val capitalizedCountry = country.split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
            if (capitalizedCountry != country) {
                updates["country"] = capitalizedCountry
            }
        }

        if (city != null) {
            val capitalizedCity = city.split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
            if (capitalizedCity != city) {
                updates["cityId"] = capitalizedCity
            }
        }

        if (updates.isNotEmpty()) {
            println("Updating trip: ${trip.id}")
            trip.reference.update(updates).get()
        }
    }
    println("Trip capitalization finished.")

    // Cleanup duplicate cities
    println("Starting duplicate city cleanup...")
    val countries = db.collection("countries").get().get()
    for (country in countries.documents) {
        val cities = country.reference.collection("cities").get().get()
        val seenCities = mutableSetOf<String>()

        for (cityDoc in cities.documents) {
            val cityName = cityDoc.id
            var canonicalName = cityName.lowercase()

            if (canonicalName.contains("rotterdam") || canonicalName.contains("rooterdam")) {
                canonicalName = "rotterdam"
            }

            if (seenCities.contains(canonicalName)) {
                println("Deleting duplicate city: $cityName in ${country.id}")
                cityDoc.reference.delete().get()
            } else {
                seenCities.add(canonicalName)
                val capitalizedCityName = cityName.split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                if (capitalizedCityName != cityName) {
                    println("NOTE: City '$cityName' in country '${country.id}' has incorrect capitalization but won't be renamed.")
                }
            }
        }
    }
    println("Duplicate cleanup finished.")
}
