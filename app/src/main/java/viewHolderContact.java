import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintly_app_smd_finale.R;

public class viewHolderContact extends RecyclerView.ViewHolder {

    ImageView imgview;
    TextView name;
    public viewHolderContact(@NonNull View itemView) {
        super(itemView);
        imgview=itemView.findViewById(R.id.contactImage);
        name=itemView.findViewById(R.id.contactName);
    }
}
