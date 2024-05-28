package com.example.cooking_social_network.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.cooking_social_network.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.autocomplete.Hashtag;
import com.hendraanggrian.appcompat.socialview.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private String imageUrl;
    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        // khi click vao close thi se chuyen sang MainActivity
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

        CropImage.activity().start(PostActivity.this);
    }

    /*Hàm này xử lý việc tải ảnh lên Firebase Storage,
    tạo bài đăng mới trong Firebase Database và điều hướng quay lại trang chính.*/
    private void upload() {

        // Tạo một hộp thoại tiến trình để hiển thị cho người dùng tiến trình tải lên.
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        // Kiểm tra xem người dùng đã chọn ảnh hay chưa.
        if (imageUri != null){

            // Tạo một tham chiếu đến thư mục "Posts" trên Firebase Storage để lưu trữ ảnh được tải lên.
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts")
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            // Tải ảnh được chọn lên Firebase Storage.
            StorageTask uploadtask = filePath.putFile(imageUri);

            // Kết nối hai tác vụ bất đồng bộ: tải ảnh và sau đó lấy URL tải xuống.
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    // Lấy URL tải xuống của ảnh đã tải lên.
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();

                    // Tạo một tham chiếu đến nút "Posts" trong Firebase Database.
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    String postId = ref.push().getKey();

                    // Tạo một map để lưu trữ dữ liệu bài đăng.
                    HashMap<String , Object> map = new HashMap<>();
                    map.put("postId" , postId);
                    map.put("imageUrl" , imageUrl);
                    map.put("description" , description.getText().toString());
                    map.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // Lưu dữ liệu bài đăng vào cơ sở dữ liệu dưới ID bài đăng duy nhất.
                    ref.child(postId).setValue(map);

                    // Tùy chọn: Lưu hashtag nếu có.
                    DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                    List<String> hashTags = description.getHashtags();
                    if (!hashTags.isEmpty()){
                        for (String tag : hashTags){
                            map.clear();

                            map.put("tag" , tag.toLowerCase());
                            map.put("postId" , postId);

                            mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                        }
                    }

                    // Thoát khỏi hộp thoại tiến trình.
                    pd.dismiss();

                    // Điều hướng quay lại trang chính và kết thúc hoạt động hiện tại.
                    startActivity(new Intent(PostActivity.this , MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                // Xử lý bất kỳ lỗi nào khác trong quá trình.
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Thông báo cho người dùng nếu họ chưa chọn ảnh để tải lên.
            Toast.makeText(this, "No image was selected!", Toast.LENGTH_SHORT).show();
        }

    }

    // Hàm này lấy phần mở rộng của file từ URI của ảnh.
    private String getFileExtension(Uri uri) {

        // Lấy đối tượng MimeTypeMap để truy cập thông tin về các loại MIME.
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Lấy loại MIME của file từ URI.
        String mimeType = this.getContentResolver().getType(uri);

        // Sử dụng loại MIME để lấy phần mở rộng của file.
        return mimeTypeMap.getExtensionFromMimeType(mimeType);
//      return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra kết quả trả về từ thư viện cắt ảnh CropImage.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Lấy kết quả cắt ảnh.
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            // Cập nhật URI của ảnh đã được cắt.
            imageUri = result.getUri();

            // Hiển thị ảnh đã cắt lên ImageView.
            imageAdded.setImageURI(imageUri);
        } else {
            // Xử lý trường hợp cắt ảnh thất bại.
            Toast.makeText(this, "Thử lại!", Toast.LENGTH_SHORT).show();

            // Điều hướng về trang chính và kết thúc hoạt động hiện tại.
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Khởi tạo bộ chuyển đổi ArrayAdapter cho danh sách Hashtag.
        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        // Lấy tham chiếu đến nút "HashTags" trong Firebase Database.
        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");

        // Thêm lắng nghe sự kiện thay đổi dữ liệu trên nút "HashTags".
        mHashTagRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Duyệt qua các Hashtag con.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy tên Hashtag và số lượng bài đăng liên quan.
                    String hashtag = snapshot.getKey();
                    int count = (int) snapshot.getChildrenCount();

                    // Thêm Hashtag mới vào bộ chuyển đổi.
                    hashtagAdapter.add(new Hashtag(hashtag, count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Thiết lập bộ chuyển đổi Hashtag cho EditText description.
        description.setHashtagAdapter(hashtagAdapter);
    }
}