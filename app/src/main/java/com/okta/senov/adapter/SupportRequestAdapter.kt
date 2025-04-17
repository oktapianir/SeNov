package com.okta.senov.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.databinding.ItemSupportRequestBinding
import com.okta.senov.model.SupportRequest
import timber.log.Timber

class SupportRequestAdapter(
    private val supportRequests: MutableList<SupportRequest> = mutableListOf(),
    private val onEditClick: (SupportRequest) -> Unit
) : RecyclerView.Adapter<SupportRequestAdapter.SupportRequestViewHolder>() {

    inner class SupportRequestViewHolder(val binding: ItemSupportRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportRequestViewHolder {
        val binding =
            ItemSupportRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupportRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupportRequestViewHolder, position: Int) {
        val currentItem = supportRequests[position]
        val b = holder.binding

        b.idSupportTextView.text = currentItem.id_support_request
        b.textViewName.text = currentItem.name
        b.textViewEmail.text = currentItem.email
//        b.textViewCategory.text = "Kategori: ${currentItem.category}"
        b.textViewDescription.text = currentItem.description
        b.textViewTimestamp.text = currentItem.timestamp.toString()
        b.textViewStatus.text = currentItem.status

        // Set warna status
        val statusColor = when (currentItem.status.lowercase()) {
            "pending" -> "#808080"
            "diproses" -> "#FFC107"
            "selesai" -> "#4CAF50"
            else -> "#808080" // Gray
        }
        b.textViewStatus.setBackgroundColor(Color.parseColor(statusColor))

        b.editButton.setOnClickListener {
            onEditClick(currentItem)
        }
    }

    override fun getItemCount() = supportRequests.size

    fun updateData(newData: List<SupportRequest>) {
        supportRequests.clear()
        supportRequests.addAll(newData)
        notifyDataSetChanged()
    }

    private fun createNewSupportRequest(
        supportRequest: SupportRequest
    ) {
        // Generate ID acak dari Firestore
        // 1. Generate ID acak sendiri
        val db = FirebaseFirestore.getInstance()
        val generatedId = db.collection("support_requests")
            .document().id // ID yang akan dipakai sebagai document ID

// 2. Buat data
        val supportRequestData = hashMapOf(
            "id_support_request" to supportRequest.id_support_request,
            "name" to supportRequest.name,
            "email" to supportRequest.email,
//            "category" to supportRequest.category,
            "description" to supportRequest.description,
            "status" to "Pending",
            "timestamp" to Timestamp.now()
        )


// 3. Simpan ke Firestore pakai ID buatan sendiri
        db.collection("support_requests").document(generatedId).set(supportRequestData)
            .addOnSuccessListener {
                Timber.tag("Firestore").d("Sukses menyimpan dengan ID: $generatedId")
            }
            .addOnFailureListener {
                Timber.tag("Firestore").e(it, "Gagal menyimpan")
            }
    }
}
