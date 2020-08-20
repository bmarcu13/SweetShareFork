package com.chocolit.sweetshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddProduct extends AppCompatActivity {

    private View chooseImgView, publishButton, loadingOverlayBackground;
    private EditText productNameField, productDescriptionField, cityField, phoneNumberField;
    private TextView userDisplayNameField, userEmailField;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private ProgressBar loadingBar;

    final ArrayList<String> uriList = new ArrayList<>();
    private Map<String, Object> productData = new HashMap<>();

    private ArrayList<Uri> imgList = new ArrayList<Uri>();

    public AddProduct() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPref = getSharedPreferences(UserConstants.USER_FETCHED_DATA_SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        userDisplayNameField = findViewById(R.id.ContactNameDisplay);
        userEmailField = findViewById(R.id.EmailAdressInputField);
        userDisplayNameField.setText(sharedPref.getString(UserConstants.USER_FULL_NAME, "Default"));
        userEmailField.setText(sharedPref.getString(UserConstants.USER_EMAIL, "Default"));

        chooseImgView = findViewById(R.id.addPhotosBackground);
        publishButton = findViewById(R.id.PublishButtonBackground);

        loadingOverlayBackground = findViewById(R.id.loadingOverlayBackground);
        loadingBar = findViewById(R.id.progressBar);

        productNameField = findViewById(R.id.TitleInputField);
        productDescriptionField = findViewById(R.id.DescriptionInputField);
        cityField = findViewById(R.id.CityInputField);
        phoneNumberField = findViewById(R.id.PhoneNumberInputField);

        chooseImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, RequestCodes.GALLERY_REQUEST_CODE);
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productTitle = productNameField.getText().toString();
                String productDescription = productDescriptionField.getText().toString();
                String city = cityField.getText().toString();
                String phoneNumber = phoneNumberField.getText().toString();

                productData.put(ProductConstants.PRODUCT_TITLE, productTitle);
                productData.put(ProductConstants.PRODUCT_DESCRIPTION, productDescription);
                productData.put(ProductConstants.PRODUCT_CITY, city);
                productData.put(ProductConstants.PRODUCT_OWNER_PHONE_NUMBER, phoneNumber);

                if (productTitle.isEmpty()){
                    productNameField.setError("This field can't be empty");
                    return;
                }
                if (productDescription.isEmpty()) {
                    productDescriptionField.setError("This field can't be empty");
                    return;
                }
                if (city.isEmpty()) {
                    cityField.setError("This field can't be empty");
                    return;
                }

//                loadingBar.setVisibility(ProgressBar.VISIBLE);
//                loadingOverlayBackground.setVisibility(View.VISIBLE);

                Date date = new Date();
                Timestamp ts = new Timestamp(date.getTime());
                final String productId = fAuth.getCurrentUser().getUid() + ts.getTime();
                StorageReference imageFolder = FirebaseStorage.getInstance().getReference().child("productImg/" + productId);

                for (int i = 0; i < imgList.size(); i++) {
                    Uri currentImg = imgList.get(i);

                    final StorageReference imageName = imageFolder.child("" + i);
                    imageName.putFile(currentImg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("TAG", "onSuccess: Uploaded files");

                            imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    uriList.add(uri.toString());

                                    if (uriList.size() == imgList.size()) {
                                        productData.put(ProductConstants.PRODUCT_IMG_LIST, uriList);
                                        DocumentReference documentReference = fStore.collection("products").document(productId);
                                        Log.d("TAG", "onSuccess: uploading to db");
                                        documentReference.set(productData);
                                    }

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "onFailure: " + e);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data.getClipData() != null) {
                    int countClipData = data.getClipData().getItemCount();

                    for (int i = 0; i < countClipData; i++) {
                        Uri imgUri = data.getClipData().getItemAt(i).getUri();
                        imgList.add(imgUri);
                    }
                }
            }
            else {

            }
        }
    }
}