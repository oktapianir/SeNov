package com.okta.senov.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.okta.senov.databinding.ItemRatingBinding
import com.okta.senov.model.Rating
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RatingAdapter(private val ratingsList: List<Rating>) :
    RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {

    class RatingViewHolder(val binding: ItemRatingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val binding = ItemRatingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RatingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val rating = ratingsList[position]

        with(holder.binding) {
            tvIdRating.text = "Id Rating: ${rating.id_rating}"
            tvBookId.text = "Book ID: ${rating.bookId}"
            tvRatingValue.text = "${rating.rating}"
            chipRecommended.text = "Recommended: ${if (rating.recommended) "Yes" else "No"}"
            tvReview.text = "${rating.review}"

            try {
                // Periksa tipe data timestamp
                Timber.tag("RatingAdapter")
                    .d("Timestamp type: ${rating.timestamp?.javaClass?.name}")

                if (rating.timestamp != null) {
                    // Konversi timestamp ke Date dengan pemeriksaan tipe
                    val date: Date? = when (rating.timestamp) {
                        is com.google.firebase.Timestamp -> (rating.timestamp as com.google.firebase.Timestamp).toDate()
                        is java.util.Date -> rating.timestamp as java.util.Date
                        is java.sql.Timestamp -> Date((rating.timestamp as java.sql.Timestamp).time)
                        is Long -> Date(rating.timestamp as Long)
                        else -> {
                            Timber.tag("RatingAdapter")
                                .e("Unknown timestamp type: ${rating.timestamp.javaClass}")
                            null
                        }
                    }

                    // Format date jika berhasil dikonversi
                    if (date != null) {
                        val dateFormat =
                            SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                        tvTimestamp.text = "Time: ${dateFormat.format(date)}"
                    } else {
                        tvTimestamp.text = "Time: (format unknown)"
                    }
                } else {
                    tvTimestamp.text = "Time: Not available"
                }
            } catch (e: Exception) {
                Timber.tag("RatingAdapter").e(e, "Error formatting timestamp: ${e.message}")
                tvTimestamp.text = "Time: Format error"
            }

            tvUserEmail.text = "User: ${rating.userEmail}"
        }
    }

    override fun getItemCount() = ratingsList.size
}