//package com.okta.senov.fragment
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.bumptech.glide.Glide
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.Timestamp
//import com.okta.senov.R
//import com.okta.senov.databinding.FragmentDetailBinding
//import com.okta.senov.model.Book
//import com.okta.senov.model.BookContent
//import timber.log.Timber
//
//class DetailFragment : Fragment() {
//    private var _binding: FragmentDetailBinding? = null
//    private val binding get() = _binding!!
//
//    private val args: DetailFragmentArgs by navArgs()
//    private val db = FirebaseFirestore.getInstance()
//    private var isBookmarked = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentDetailBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val book = args.bookArg
//        setupBookDetails(book)
//        setupButtons(book)
//        checkIfBookmarked(book.id)
//
//        // Initialize user bookmark collection if user is logged in
//        FirebaseAuth.getInstance().currentUser?.let { user ->
//            initializeBookmarkCollection(user.uid)
//        }
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        Timber.tag("Auth").d("Current user: ${currentUser?.uid ?: "Not logged in"}")
////        if (currentUser == null) {
////            // User not authenticated, show appropriate UI
////            showToast("You must be logged in to use bookmarks")
////        }
//    }
//
//    private fun setupButtons(book: Book) {
//        setupBackButton()
//
//        // Setup bookmark button
//        binding.bookmarkButton.setOnClickListener {
//            if (isBookmarked) {
//                removeBookmark(book.id)
//            } else {
//                saveBookmark(book)
//            }
//        }
//
//        binding.listenButton.setOnClickListener {
//            val bookContent = BookContent(
//                bookId = book.id,
//                title = book.title,
//                chapters = emptyList()
//            )
//            val action = DetailFragmentDirections.actionDetailToBookreader(bookContent)
//            findNavController().navigate(action)
//        }
//    }
//
//    private fun initializeBookmarkCollection(userId: String) {
//        // Create user document if it doesn't exist
//        val userDocRef = db.collection("users").document(userId)
//
//        userDocRef.get()
//            .addOnSuccessListener { document ->
//                if (!document.exists()) {
//                    // Create user document with basic data
//                    val userData = hashMapOf(
//                        "userId" to userId,
//                        "createdAt" to Timestamp.now(),
//                        "bookmarksCount" to 0
//                    )
//
//                    userDocRef.set(userData)
//                        .addOnSuccessListener {
//                            Timber.tag("Firestore").d("User document created successfully")
//
//                            // Create an empty document in bookmarks subcollection to ensure it exists
//                            userDocRef.collection("bookmarks").document("placeholder")
//                                .set(hashMapOf("placeholder" to true))
//                                .addOnSuccessListener {
//                                    Timber.tag("Firestore").d("Bookmarks collection initialized")
//                                    // Delete the placeholder after creating the collection
//                                    userDocRef.collection("bookmarks").document("placeholder")
//                                        .delete()
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Firestore").e("Error creating user document: ${e.message}")
//                        }
//                } else {
//                    // Make sure bookmarks collection exists for existing users too
//                    userDocRef.collection("bookmarks").document("placeholder")
//                        .set(hashMapOf("placeholder" to true))
//                        .addOnSuccessListener {
//                            Timber.tag("Firestore").d("Bookmarks collection initialized/verified")
//                            // Delete the placeholder after verifying/creating the collection
//                            userDocRef.collection("bookmarks").document("placeholder").delete()
//                        }
//                }
//            }
//            .addOnFailureListener { e ->
//                Timber.tag("Firestore").e("Error checking user document: ${e.message}")
//            }
//    }
//
//    // Check if book is already bookmarked
//    private fun checkIfBookmarked(bookId: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(bookId)
//                .get()
//                .addOnSuccessListener { document ->
//                    // Check if fragment is still attached to context before proceeding
//                    if (!isAdded || _binding == null) return@addOnSuccessListener
//
//                    isBookmarked = document.exists()
//                    updateBookmarkIcon()
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("Bookmark").e("Error checking bookmark: ${e.message}")
//                }
//        }
//    }
//
//    // Save book to user's bookmarks
//    private fun saveBookmark(book: Book) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//        Timber.tag("BookmarkDebug").d("Current user ID: $userId")
//
//        if (userId != null) {
//            // Add timestamp when bookmark was created
//            val bookData = book.toMap().toMutableMap().apply {
//                put("bookmarkedAt", Timestamp.now())
//            }
//
//            Timber.tag("BookmarkDebug").d("Book data to save: $bookData")
//            Timber.tag("BookmarkDebug").d("Saving to path: users/$userId/bookmarks/${book.id}")
//
//            // Save book to bookmarks collection
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(book.id)
//                .set(bookData)
//                .addOnSuccessListener {
//                    isBookmarked = true
//                    updateBookmarkIcon()
//
//                    Timber.tag("BookmarkDebug")
//                        .d("SUCCESS: Document written at users/$userId/bookmarks/${book.id}")
//
//                    // Update bookmark counter in user document
//                    db.collection("users").document(userId)
//                        .update("bookmarksCount", FieldValue.increment(1))
//                        .addOnSuccessListener {
//                            Timber.tag("Bookmark").d("Bookmark counter updated")
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Bookmark")
//                                .e("Error updating bookmark counter: ${e.message}")
//                        }
//
//                    showToast("Book added to your collection")
//                    Timber.tag("Bookmark").d("Book bookmarked: ${book.title}")
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("BookmarkDebug")
//                        .e("FAILURE: Error adding bookmark: ${e.message}, ${e.stackTraceToString()}")
//                    showToast("Failed to bookmark book")
//                }
//        } else {
//            // Handle case where user is not logged in
//            Timber.tag("BookmarkDebug").e("User is not logged in!")
//            showToast("Please login first!!")
//        }
//    }
//
//    // Remove book from bookmarks
//    private fun removeBookmark(bookId: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(bookId)
//                .delete()
//                .addOnSuccessListener {
//                    isBookmarked = false
//                    updateBookmarkIcon()
//
//                    // Update counter bookmark di dokumen user
//                    db.collection("users").document(userId)
//                        .update("bookmarksCount", FieldValue.increment(-1))
//                        .addOnSuccessListener {
//                            Timber.tag("Bookmark").d("Bookmark counter updated")
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Bookmark")
//                                .e("Error updating bookmark counter: ${e.message}")
//                        }
//
//                    showToast("Book removed from your collection")
//                    Timber.tag("Bookmark").d("Book removed from bookmarks: $bookId")
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("Bookmark").e("Error removing bookmark: ${e.message}")
//                    showToast("Failed to remove bookmark")
//                }
//        }
//    }
//
//    // Get all bookmarks for current user
//    fun getAllBookmarks(callback: (List<Book>) -> Unit) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks")
//                .orderBy("bookmarkedAt") // Sort by bookmark date
//                .get()
//                .addOnSuccessListener { result ->
//                    val bookmarkList = ArrayList<Book>()
//                    for (document in result) {
//                        try {
//                            val book = document.toObject(Book::class.java)
//                            bookmarkList.add(book)
//                        } catch (e: Exception) {
//                            Timber.tag("Bookmark").e("Error converting document: ${e.message}")
//                        }
//                    }
//                    callback(bookmarkList)
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("Bookmark").e("Error getting bookmarks: ${e.message}")
//                    callback(emptyList())
//                }
//        } else {
//            callback(emptyList())
//        }
//    }
//
//    // Update bookmark icon based on current state
//    private fun updateBookmarkIcon() {
//        if (_binding == null) return
//
//        val iconResource = if (isBookmarked) {
//            R.drawable.ic_bookmark_filled
//        } else {
//            R.drawable.ic_bookmark
//        }
//        binding.bookmarkButton.setImageResource(iconResource)
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun setupBookDetails(book: Book) {
//        binding.apply {
//            bookTitle.text = book.title
//            authorName.text = book.authorName
//            bookDescriptionTextView.text = book.description
//
//            Timber.tag("ImageLoading").d("Mencoba memuat gambar dari URL: ${book.image}")
//
//            Glide.with(bookCoverImageView.context)
//                .load(book.image)
//                .error(R.drawable.ic_error)
//                .into(bookCoverImageView)
//        }
//
//        // Log the book details
//        Timber.tag("BookDetails")
//            .d("Book: ID=${book.id}, Title=${book.title}, Author=${book.authorName}, Description=${book.description}")
//
//        // Only fetch from Firestore if description is empty
//        if (book.description.isBlank()) {
//            fetchBookDetails(book.id)
//        }
//
//        setupBackButton()
//    }
//
//    private fun fetchBookDetails(bookId: String) {
//        // Add logging to check the bookId value
//        Timber.tag("Firestore").d("Attempting to fetch book with ID: $bookId")
//
//        db.collection("Books").document(bookId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    // Log the entire document for debugging
//                    Timber.tag("Firestore").d("Document data: ${document.data}")
//
//                    val author = document.getString("nameAuthor") ?: "Tidak ada data"
//                    val description =
//                        document.getString("bookDescription") ?: "Deskripsi tidak tersedia"
//                    val imageUrl = document.getString("imageUrl")
//
//                    binding.authorName.text = author
//                    binding.bookDescriptionTextView.text = description
//
//                    if (!imageUrl.isNullOrEmpty()) {
//                        Timber.tag("ImageLoading").d("Memuat gambar dari Firestore URL: $imageUrl")
//
//                        Glide.with(binding.bookCoverImageView.context)
//                            .load(imageUrl)
//                            .error(R.drawable.ic_error)
//                            .into(binding.bookCoverImageView)
//                    }
//                    Timber.tag("Firestore").d("Data successfully retrieved: $author - $description")
//                } else {
//                    Timber.tag("Firestore").e("Document not found")
//                    binding.authorName.visibility = View.GONE
//                    binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
//                }
//            }
//            .addOnFailureListener { e ->
//                Timber.tag("FirestoreError").e("Failed to retrieve data: ${e.message}")
//                binding.authorName.visibility = View.GONE
//                binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
//            }
//    }
//
//    private fun setupBackButton() {
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().popBackStack()
//                }
//            }
//        )
//        binding.backButton.setOnClickListener {
//            findNavController().popBackStack()
//        }
//        val book = args.bookArg
//        binding.listenButton.setOnClickListener {
//            val bookContent = BookContent(
//                bookId = book.id,
//                title = book.title,
//                chapters = emptyList()
//            )
//            val action = DetailFragmentDirections.actionDetailToBookreader(bookContent)
//            findNavController().navigate(action)
//        }
//    }
//
//    // Helper extension function to convert Book to Map for Firestore
//    private fun Book.toMap(): Map<String, Any?> {
//        return mapOf(
//            "id" to id,
//            "title" to title,
//            "authorName" to authorName,
//            "description" to description,
//            "image" to image
//        )
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

//package com.okta.senov.fragment
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.bumptech.glide.Glide
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.Timestamp
//import com.okta.senov.R
//import com.okta.senov.databinding.FragmentDetailBinding
//import com.okta.senov.model.Book
//import com.okta.senov.model.BookContent
//import timber.log.Timber
//
//class DetailFragment : Fragment() {
//    private var _binding: FragmentDetailBinding? = null
//    private val binding get() = _binding!!
//
//    private val args: DetailFragmentArgs by navArgs()
//    private val db = FirebaseFirestore.getInstance()
//    private var isBookmarked = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentDetailBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val book = args.bookArg
//        setupBookDetails(book)
//        setupButtons(book)
//        checkIfBookmarked(book.id)
//
//        // Initialize user bookmark collection if user is logged in
//        FirebaseAuth.getInstance().currentUser?.let { user ->
//            initializeBookmarkCollection(user.uid)
//        }
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        Timber.tag("Auth").d("Current user: ${currentUser?.uid ?: "Not logged in"}")
////        if (currentUser == null) {
////            // User not authenticated, show appropriate UI
////            showToast("You must be logged in to use bookmarks")
////        }
//    }
//
//    private fun setupButtons(book: Book) {
//        setupBackButton()
//
//        // Setup bookmark button
//        binding.bookmarkButton.setOnClickListener {
//            if (isBookmarked) {
//                removeBookmark(book.id)
//            } else {
//                saveBookmark(book)
//            }
//        }
//
//        binding.listenButton.setOnClickListener {
//            val bookContent = BookContent(
//                bookId = book.id,
//                title = book.title,
//                chapters = emptyList()
//            )
//            val action = DetailFragmentDirections.actionDetailToBookreader(bookContent)
//            findNavController().navigate(action)
//        }
//    }
//
//    private fun initializeBookmarkCollection(userId: String) {
//        // Create user document if it doesn't exist
//        val userDocRef = db.collection("users").document(userId)
//
//        userDocRef.get()
//            .addOnSuccessListener { document ->
//                if (!document.exists()) {
//                    // Create user document with basic data
//                    val userData = hashMapOf(
//                        "userId" to userId,
//                        "createdAt" to Timestamp.now(),
//                        "bookmarksCount" to 0
//                    )
//
//                    userDocRef.set(userData)
//                        .addOnSuccessListener {
//                            Timber.tag("Firestore").d("User document created successfully")
//
//                            // Create an empty document in bookmarks subcollection to ensure it exists
//                            userDocRef.collection("bookmarks").document("placeholder")
//                                .set(hashMapOf("placeholder" to true))
//                                .addOnSuccessListener {
//                                    Timber.tag("Firestore").d("Bookmarks collection initialized")
//                                    // Delete the placeholder after creating the collection
//                                    userDocRef.collection("bookmarks").document("placeholder")
//                                        .delete()
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Firestore").e("Error creating user document: ${e.message}")
//                        }
//                } else {
//                    // Make sure bookmarks collection exists for existing users too
//                    userDocRef.collection("bookmarks").document("placeholder")
//                        .set(hashMapOf("placeholder" to true))
//                        .addOnSuccessListener {
//                            Timber.tag("Firestore").d("Bookmarks collection initialized/verified")
//                            // Delete the placeholder after verifying/creating the collection
//                            userDocRef.collection("bookmarks").document("placeholder").delete()
//                        }
//                }
//            }
//            .addOnFailureListener { e ->
//                Timber.tag("Firestore").e("Error checking user document: ${e.message}")
//            }
//    }
//
//    // Check if book is already bookmarked
//    private fun checkIfBookmarked(bookId: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(bookId)
//                .get()
//                .addOnSuccessListener { document ->
//                    // Check if fragment is still attached to context before proceeding
//                    if (!isAdded || _binding == null) return@addOnSuccessListener
//
//                    isBookmarked = document.exists()
//                    updateBookmarkIcon()
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("Bookmark").e("Error checking bookmark: ${e.message}")
//                }
//        }
//    }
//
//    // Save book to user's bookmarks
//    private fun saveBookmark(book: Book) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//        Timber.tag("BookmarkDebug").d("Current user ID: $userId")
//
//        if (userId != null) {
//            // Add timestamp when bookmark was created
//            val bookData = book.toMap().toMutableMap().apply {
//                put("bookmarkedAt", Timestamp.now())
//            }
//
//            Timber.tag("BookmarkDebug").d("Book data to save: $bookData")
//            Timber.tag("BookmarkDebug").d("Saving to path: users/$userId/bookmarks/${book.id}")
//
//            // Save book to bookmarks collection
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(book.id)
//                .set(bookData)
//                .addOnSuccessListener {
//                    isBookmarked = true
//                    updateBookmarkIcon()
//
//                    Timber.tag("BookmarkDebug")
//                        .d("SUCCESS: Document written at users/$userId/bookmarks/${book.id}")
//
//                    // Update bookmark counter in user document
//                    db.collection("users").document(userId)
//                        .update("bookmarksCount", FieldValue.increment(1))
//                        .addOnSuccessListener {
//                            Timber.tag("Bookmark").d("Bookmark counter updated")
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Bookmark")
//                                .e("Error updating bookmark counter: ${e.message}")
//                        }
//
//                    showToast("Book added to your collection")
//                    Timber.tag("Bookmark").d("Book bookmarked: ${book.title}")
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("BookmarkDebug")
//                        .e("FAILURE: Error adding bookmark: ${e.message}, ${e.stackTraceToString()}")
//                    showToast("Failed to bookmark book")
//                }
//        } else {
//            // Handle case where user is not logged in
//            Timber.tag("BookmarkDebug").e("User is not logged in!")
//            showToast("Please login first!!")
//        }
//    }
//
//    // Remove book from bookmarks
//    private fun removeBookmark(bookId: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(bookId)
//                .delete()
//                .addOnSuccessListener {
//                    isBookmarked = false
//                    updateBookmarkIcon()
//
//                    // Update counter bookmark di dokumen user
//                    db.collection("users").document(userId)
//                        .update("bookmarksCount", FieldValue.increment(-1))
//                        .addOnSuccessListener {
//                            Timber.tag("Bookmark").d("Bookmark counter updated")
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("Bookmark")
//                                .e("Error updating bookmark counter: ${e.message}")
//                        }
//
//                    showToast("Book removed from your collection")
//                    Timber.tag("Bookmark").d("Book removed from bookmarks: $bookId")
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("Bookmark").e("Error removing bookmark: ${e.message}")
//                    showToast("Failed to remove bookmark")
//                }
//        }
//    }
//
//    // Get all bookmarks for current user
//    fun getAllBookmarks(callback: (List<Book>) -> Unit) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks")
//                .orderBy("bookmarkedAt") // Sort by bookmark date
//                .get()
//                .addOnSuccessListener { result ->
//                    val bookmarkList = ArrayList<Book>()
//                    for (document in result) {
//                        try {
//                            val book = document.toObject(Book::class.java)
//                            bookmarkList.add(book)
//                        } catch (e: Exception) {
//                            Timber.tag("Bookmark").e("Error converting document: ${e.message}")
//                        }
//                    }
//                    callback(bookmarkList)
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("Bookmark").e("Error getting bookmarks: ${e.message}")
//                    callback(emptyList())
//                }
//        } else {
//            callback(emptyList())
//        }
//    }
//
//    // Update bookmark icon based on current state
//    private fun updateBookmarkIcon() {
//        if (_binding == null) return
//
//        val iconResource = if (isBookmarked) {
//            R.drawable.ic_bookmark_filled
//        } else {
//            R.drawable.ic_bookmark
//        }
//        binding.bookmarkButton.setImageResource(iconResource)
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun setupBookDetails(book: Book) {
//        binding.apply {
//            bookTitle.text = book.title
//            authorName.text = book.authorName
//            bookDescriptionTextView.text = book.description
//
//            Timber.tag("ImageLoading").d("Mencoba memuat gambar dari URL: ${book.image}")
//
//            Glide.with(bookCoverImageView.context)
//                .load(book.image)
//                .error(R.drawable.ic_error)
//                .into(bookCoverImageView)
//        }
//
//        // Log the book details
//        Timber.tag("BookDetails")
//            .d("Book: ID=${book.id}, Title=${book.title}, Author=${book.authorName}, Description=${book.description}")
//
//        // Only fetch from Firestore if description is empty
//        if (book.description.isBlank()) {
//            fetchBookDetails(book.id)
//        }
//
//        setupBackButton()
//    }
//
//    private fun fetchBookDetails(bookId: String) {
//        // Add logging to check the bookId value
//        Timber.tag("Firestore").d("Attempting to fetch book with ID: $bookId")
//
//        db.collection("Books").document(bookId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    // Log the entire document for debugging
//                    Timber.tag("Firestore").d("Document data: ${document.data}")
//
//                    val author = document.getString("nameAuthor") ?: "Tidak ada data"
//                    val description =
//                        document.getString("bookDescription") ?: "Deskripsi tidak tersedia"
//                    val imageUrl = document.getString("imageUrl")
//
//                    binding.authorName.text = author
//                    binding.bookDescriptionTextView.text = description
//
//                    if (!imageUrl.isNullOrEmpty()) {
//                        Timber.tag("ImageLoading").d("Memuat gambar dari Firestore URL: $imageUrl")
//
//                        Glide.with(binding.bookCoverImageView.context)
//                            .load(imageUrl)
//                            .error(R.drawable.ic_error)
//                            .into(binding.bookCoverImageView)
//                    }
//                    Timber.tag("Firestore").d("Data successfully retrieved: $author - $description")
//                } else {
//                    Timber.tag("Firestore").e("Document not found")
//                    binding.authorName.visibility = View.GONE
//                    binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
//                }
//            }
//            .addOnFailureListener { e ->
//                Timber.tag("FirestoreError").e("Failed to retrieve data: ${e.message}")
//                binding.authorName.visibility = View.GONE
//                binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
//            }
//    }
//
//    private fun setupBackButton() {
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().popBackStack()
//                }
//            }
//        )
//        binding.backButton.setOnClickListener {
//            findNavController().popBackStack()
//        }
//        val book = args.bookArg
//        binding.listenButton.setOnClickListener {
//            val bookContent = BookContent(
//                bookId = book.id,
//                title = book.title,
//                chapters = emptyList()
//            )
//            val action = DetailFragmentDirections.actionDetailToBookreader(bookContent)
//            findNavController().navigate(action)
//        }
//    }
//
//    // Helper extension function to convert Book to Map for Firestore
//    private fun Book.toMap(): Map<String, Any?> {
//        return mapOf(
//            "id" to id,
//            "title" to title,
//            "authorName" to authorName,
//            "description" to description,
//            "image" to image
//        )
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}


package com.okta.senov.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.okta.senov.R
import com.okta.senov.databinding.FragmentDetailBinding
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import com.okta.senov.viewmodel.YourBookViewModel
import timber.log.Timber

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DetailFragmentArgs by navArgs()
    private val db = FirebaseFirestore.getInstance()
    private var isBookmarked = false
    private val yourBookViewModel: YourBookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val book = args.bookArg
        setupBookDetails(book)
        setupButtons(book)
        checkIfBookmarked(book.id)

        // Initialize user bookmark collection if user is logged in
        FirebaseAuth.getInstance().currentUser?.let { user ->
            initializeCollections(user.uid)
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        Timber.tag("Auth").d("Current user: ${currentUser?.uid ?: "Not logged in"}")
    }

    private fun setupButtons(book: Book) {
        setupBackButton()

        // Setup bookmark button
        binding.bookmarkButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                showToast("Please login first!!")
                return@setOnClickListener
            }

            if (isBookmarked) {
                removeBookmark(book.id)
            } else {
                saveBookmark(book)
            }
        }

        binding.listenButton.setOnClickListener {
            // Tambahkan buku ke data_read_listened collection
            addToReadListenedData(book)

            // Navigasi ke BookReader
            val bookContent = BookContent(
                bookId = book.id,
                title = book.title,
                chapters = emptyList()
            )
            val action = DetailFragmentDirections.actionDetailToBookreader(bookContent)
            findNavController().navigate(action)
        }
    }

    private fun initializeCollections(userId: String) {
        // Create user document if it doesn't exist
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create user document with basic data
                    val userData = hashMapOf(
                        "userId" to userId,
                        "createdAt" to Timestamp.now(),
                        "bookmarksCount" to 0
                    )

                    userDocRef.set(userData)
                        .addOnSuccessListener {
                            Timber.tag("Firestore").d("User document created successfully")
                            // Initialize all required collections
                            initializeBookmarkCollection(userDocRef)
                            initializeReadListenedCollection()
                            initializeYourBooksCollection(userDocRef)
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("Firestore").e("Error creating user document: ${e.message}")
                        }
                } else {
                    // For existing users, verify all collections exist
                    initializeBookmarkCollection(userDocRef)
                    initializeReadListenedCollection()
                    initializeYourBooksCollection(userDocRef)
                }
            }
            .addOnFailureListener { e ->
                Timber.tag("Firestore").e("Error checking user document: ${e.message}")
            }
    }

    private fun initializeBookmarkCollection(userDocRef: com.google.firebase.firestore.DocumentReference) {
        // Create an empty document in bookmarks subcollection to ensure it exists
        userDocRef.collection("bookmarks").document("placeholder")
            .set(hashMapOf("placeholder" to true))
            .addOnSuccessListener {
                Timber.tag("Firestore").d("Bookmarks collection initialized")
                // Delete the placeholder after creating the collection
                userDocRef.collection("bookmarks").document("placeholder")
                    .delete()
            }
            .addOnFailureListener { e ->
                Timber.tag("Firestore").e("Error initializing bookmarks collection: ${e.message}")
            }
    }

    private fun initializeReadListenedCollection() {
        // Initialize data_read_listened collection
        db.collection("data_read_listened").document("placeholder")
            .set(hashMapOf("placeholder" to true))
            .addOnSuccessListener {
                Timber.tag("Firestore").d("data_read_listened collection initialized/verified")
                // Delete the placeholder after creating the collection
                db.collection("data_read_listened").document("placeholder").delete()
            }
            .addOnFailureListener { e ->
                Timber.tag("Firestore").e("Error initializing data_read_listened: ${e.message}")
            }
    }

    private fun initializeYourBooksCollection(userDocRef: com.google.firebase.firestore.DocumentReference) {
        // Initialize yourBooks subcollection in user document
        userDocRef.collection("yourBooks").document("placeholder")
            .set(hashMapOf("placeholder" to true))
            .addOnSuccessListener {
                Timber.tag("Firestore").d("yourBooks collection initialized/verified")
                // Delete the placeholder after creating the collection
                userDocRef.collection("yourBooks").document("placeholder").delete()
            }
            .addOnFailureListener { e ->
                Timber.tag("Firestore").e("Error initializing yourBooks: ${e.message}")
            }
    }

    // Check if book is already bookmarked
    private fun checkIfBookmarked(bookId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("bookmarks").document(bookId)
                .get()
                .addOnSuccessListener { document ->
                    // Check if fragment is still attached to context before proceeding
                    if (!isAdded || _binding == null) return@addOnSuccessListener

                    isBookmarked = document.exists()
                    updateBookmarkIcon()
                }
                .addOnFailureListener { e ->
                    Timber.tag("Bookmark").e("Error checking bookmark: ${e.message}")
                }
        }
    }

    // Add or update book in data_read_listened collection
    private fun addToReadListenedData(book: Book) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Generate a unique IdDataRead
        val idDataRead = "DR_${userId.take(5)}_${book.id}_${System.currentTimeMillis()}"

        // Document ID akan menjadi kombinasi user ID dan book ID
        val documentId = "${userId}_${book.id}"
        val readListenedRef = db.collection("data_read_listened").document(documentId)

        // Cek apakah dokumen sudah ada
        readListenedRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Jika dokumen sudah ada, increment read count dan update timestamp
                    readListenedRef.update(
                        mapOf(
                            "readCount" to FieldValue.increment(1),
                            "lastReadAt" to Timestamp.now()
                        )
                    )
                        .addOnSuccessListener {
                            Timber.tag("ReadListened").d("Book read count incremented")
                            val currentCount = document.getLong("readCount")?.plus(1) ?: 1
                            showToast("Added to Your Books (Read ${currentCount}x)")

                            // Hapus dari hiddenBooks jika ada
                            db.collection("users").document(userId)
                                .collection("hiddenBooks").document(book.id)
                                .delete()
                                .addOnSuccessListener {
                                    Timber.tag("DetailFragment").d("Buku dihapus dari hidden: ${book.id}")
                                    // Trigger refresh di YourBookViewModel
                                    yourBookViewModel.triggerRefresh()
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("ReadListened").e("Error updating read count: ${e.message}")
                            showToast("Failed to update read count")
                        }
                } else {
                    // Jika dokumen belum ada, buat dokumen baru
                    val bookData = book.toMap().toMutableMap().apply {
                        put("userId", userId)
                        put("bookId", book.id)
                        put("IdDataRead", idDataRead)
                        put("readCount", 1)
                        put("firstReadAt", Timestamp.now())
                        put("lastReadAt", Timestamp.now())
                    }

                    readListenedRef.set(bookData)
                        .addOnSuccessListener {
                            Timber.tag("ReadListened").d("Book added to data_read_listened")
                            showToast("Added to Your Books (Read 1x)")

                            // Hapus dari hiddenBooks jika ada
                            db.collection("users").document(userId)
                                .collection("hiddenBooks").document(book.id)
                                .delete()
                                .addOnSuccessListener {
                                    Timber.tag("DetailFragment").d("Buku dihapus dari hidden: ${book.id}")
                                    // Trigger refresh di YourBookViewModel
                                    yourBookViewModel.triggerRefresh()
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("ReadListened")
                                .e("Error adding to data_read_listened: ${e.message}")
                            showToast("Failed to add to Your Books")
                        }
                }

                // Tambahkan juga ke "your book" di profil user untuk tampilan cepat
                saveToUserBookshelfCollection(userId, book)
            }
            .addOnFailureListener { e ->
                Timber.tag("ReadListened")
                    .e("Error checking data_read_listened document: ${e.message}")
                showToast("Failed to process request")
            }
    }

    // Helper method untuk menambahkan buku ke koleksi yourBooks user
    private fun saveToUserBookshelfCollection(userId: String, book: Book) {
        val yourBooksRef = db.collection("users").document(userId)
            .collection("yourBooks").document(book.id)

        // Cek dulu apakah sudah ada
        yourBooksRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Update last accessed timestamp
                    yourBooksRef.update("lastAccessedAt", Timestamp.now())
                        .addOnSuccessListener {
                            Timber.tag("YourBooks").d("Updated timestamp in yourBooks collection")
                        }
                } else {
                    // Tambahkan ke yourBooks collection
                    val bookData = book.toMap().toMutableMap().apply {
                        put("addedAt", Timestamp.now())
                        put("lastAccessedAt", Timestamp.now())
                    }

                    yourBooksRef.set(bookData)
                        .addOnSuccessListener {
                            Timber.tag("YourBooks").d("Book added to yourBooks collection")
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("YourBooks").e("Error adding to yourBooks: ${e.message}")
                        }
                }
            }
    }

    // Save book to user's bookmarks
    private fun saveBookmark(book: Book) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        Timber.tag("BookmarkDebug").d("Current user ID: $userId")

        if (userId != null) {
            try {
                // Add timestamp when bookmark was created
                val bookData = book.toMap().toMutableMap().apply {
                    put("bookmarkedAt", Timestamp.now())
                }

                Timber.tag("BookmarkDebug").d("Book data to save: $bookData")
                Timber.tag("BookmarkDebug").d("Saving to path: users/$userId/bookmarks/${book.id}")

                // Hapus dari hiddenBooks jika sebelumnya dihapus
                db.collection("users").document(userId)
                    .collection("hiddenBooks").document(book.id)
                    .delete()
                    .addOnSuccessListener {
                        Timber.tag("DetailFragment").d("Buku dihapus dari hidden saat bookmark: ${book.id}")
                    }

                // Save book to bookmarks collection
                db.collection("users").document(userId)
                    .collection("bookmarks").document(book.id)
                    .set(bookData)
                    .addOnSuccessListener {
                        if (!isAdded || _binding == null) return@addOnSuccessListener

                        isBookmarked = true
                        updateBookmarkIcon()

                        Timber.tag("BookmarkDebug")
                            .d("SUCCESS: Document written at users/$userId/bookmarks/${book.id}")

                        // Update bookmark counter in user document
                        db.collection("users").document(userId)
                            .update("bookmarksCount", FieldValue.increment(1))
                            .addOnSuccessListener {
                                Timber.tag("Bookmark").d("Bookmark counter updated")
                                // Trigger refresh di YourBookViewModel
                                yourBookViewModel.triggerRefresh()
                            }
                            .addOnFailureListener { e ->
                                Timber.tag("Bookmark")
                                    .e("Error updating bookmark counter: ${e.message}")
                            }

                        showToast("Book added to your collection")
                        Timber.tag("Bookmark").d("Book bookmarked: ${book.title}")
                    }
                    .addOnFailureListener { e ->
                        if (!isAdded) return@addOnFailureListener

                        Timber.tag("BookmarkDebug")
                            .e("FAILURE: Error adding bookmark: ${e.message}, ${e.stackTraceToString()}")
                        showToast("Failed to bookmark book")
                    }
            } catch (e: Exception) {
                Timber.tag("BookmarkDebug").e("Exception during bookmarking: ${e.message}, ${e.stackTraceToString()}")
                if (isAdded) {
                    showToast("An error occurred while bookmarking")
                }
            }
        } else {
            // Handle case where user is not logged in
            Timber.tag("BookmarkDebug").e("User is not logged in!")
            showToast("Please login first!!")
        }
    }


    // Remove book from bookmarks
    private fun removeBookmark(bookId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                db.collection("users").document(userId)
                    .collection("bookmarks").document(bookId)
                    .delete()
                    .addOnSuccessListener {
                        if (!isAdded || _binding == null) return@addOnSuccessListener

                        isBookmarked = false
                        updateBookmarkIcon()

                        // Update counter bookmark di dokumen user
                        db.collection("users").document(userId)
                            .update("bookmarksCount", FieldValue.increment(-1))
                            .addOnSuccessListener {
                                Timber.tag("Bookmark").d("Bookmark counter updated")
                            }
                            .addOnFailureListener { e ->
                                Timber.tag("Bookmark")
                                    .e("Error updating bookmark counter: ${e.message}")
                            }

                        showToast("Book removed from your collection")
                        Timber.tag("Bookmark").d("Book removed from bookmarks: $bookId")
                    }
                    .addOnFailureListener { e ->
                        if (!isAdded) return@addOnFailureListener

                        Timber.tag("Bookmark").e("Error removing bookmark: ${e.message}")
                        showToast("Failed to remove bookmark")
                    }
            } catch (e: Exception) {
                Timber.tag("BookmarkDebug").e("Exception during bookmark removal: ${e.message}, ${e.stackTraceToString()}")
                if (isAdded) {
                    showToast("An error occurred while removing bookmark")
                }
            }
        }
    }

    // Get all bookmarks for current user
    fun getAllBookmarks(callback: (List<Book>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("bookmarks")
                .orderBy("bookmarkedAt") // Sort by bookmark date
                .get()
                .addOnSuccessListener { result ->
                    val bookmarkList = ArrayList<Book>()
                    for (document in result) {
                        try {
                            val book = document.toObject(Book::class.java)
                            bookmarkList.add(book)
                        } catch (e: Exception) {
                            Timber.tag("Bookmark").e("Error converting document: ${e.message}")
                        }
                    }
                    callback(bookmarkList)
                }
                .addOnFailureListener { e ->
                    Timber.tag("Bookmark").e("Error getting bookmarks: ${e.message}")
                    callback(emptyList())
                }
        } else {
            callback(emptyList())
        }
    }

    // Get all your books for current user
    fun getAllYourBooks(callback: (List<Pair<Book, Int>>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("yourBooks")
                .orderBy("lastAccessedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val yourBooksList = ArrayList<Pair<Book, Int>>()
                    val bookIds = result.documents.map { it.id }

                    // If no books found, return empty list
                    if (bookIds.isEmpty()) {
                        callback(emptyList())
                        return@addOnSuccessListener
                    }

                    // For each book, get the read count from data_read_listened
                    for (document in result) {
                        try {
                            val book = document.toObject(Book::class.java)
                            val documentId = "${userId}_${book.id}"

                            // Get read count from data_read_listened
                            db.collection("data_read_listened").document(documentId)
                                .get()
                                .addOnSuccessListener { readDoc ->
                                    val readCount = readDoc.getLong("readCount")?.toInt() ?: 0
                                    yourBooksList.add(Pair(book, readCount))

                                    // When all books processed, return the result
                                    if (yourBooksList.size == bookIds.size) {
                                        callback(yourBooksList)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Timber.tag("YourBooks")
                                        .e("Error getting read count: ${e.message}")
                                    yourBooksList.add(Pair(book, 0))

                                    // When all books processed, return the result
                                    if (yourBooksList.size == bookIds.size) {
                                        callback(yourBooksList)
                                    }
                                }
                        } catch (e: Exception) {
                            Timber.tag("YourBooks").e("Error converting document: ${e.message}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Timber.tag("YourBooks").e("Error getting your books: ${e.message}")
                    callback(emptyList())
                }
        } else {
            callback(emptyList())
        }
    }

    // Update bookmark icon based on current state
    private fun updateBookmarkIcon() {
        if (_binding == null) return

        val iconResource = if (isBookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark
        }
        binding.bookmarkButton.setImageResource(iconResource)
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBookDetails(book: Book) {
        binding.apply {
            bookTitle.text = book.title
            authorName.text = book.authorName
            bookDescriptionTextView.text = book.description

            Timber.tag("ImageLoading").d("Mencoba memuat gambar dari URL: ${book.image}")

            Glide.with(bookCoverImageView.context)
                .load(book.image)
                .error(R.drawable.ic_error)
                .into(bookCoverImageView)
        }

        // Log the book details
        Timber.tag("BookDetails")
            .d("Book: ID=${book.id}, Title=${book.title}, Author=${book.authorName}, Description=${book.description}")

        // Only fetch from Firestore if description is empty
        if (book.description.isBlank()) {
            fetchBookDetails(book.id)
        }
    }

    private fun fetchBookDetails(bookId: String) {
        // Add logging to check the bookId value
        Timber.tag("Firestore").d("Attempting to fetch book with ID: $bookId")

        db.collection("Books").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                if (document.exists()) {
                    // Log the entire document for debugging
                    Timber.tag("Firestore").d("Document data: ${document.data}")

                    val author = document.getString("nameAuthor") ?: "Tidak ada data"
                    val description =
                        document.getString("bookDescription") ?: "Deskripsi tidak tersedia"
                    val imageUrl = document.getString("imageUrl")

                    binding.authorName.text = author
                    binding.bookDescriptionTextView.text = description

                    if (!imageUrl.isNullOrEmpty()) {
                        Timber.tag("ImageLoading").d("Memuat gambar dari Firestore URL: $imageUrl")

                        Glide.with(binding.bookCoverImageView.context)
                            .load(imageUrl)
                            .error(R.drawable.ic_error)
                            .into(binding.bookCoverImageView)
                    }
                    Timber.tag("Firestore").d("Data successfully retrieved: $author - $description")
                } else {
                    Timber.tag("Firestore").e("Document not found")
                    binding.authorName.visibility = View.GONE
                    binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded || _binding == null) return@addOnFailureListener

                Timber.tag("FirestoreError").e("Failed to retrieve data: ${e.message}")
                binding.authorName.visibility = View.GONE
                binding.bookDescriptionTextView.text = getString(R.string.desc_empty)
            }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Helper extension function to convert Book to Map for Firestore
    private fun Book.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "authorName" to authorName,
            "description" to description,
            "image" to image
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}