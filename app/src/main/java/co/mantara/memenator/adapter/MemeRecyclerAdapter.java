package co.mantara.memenator.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import co.mantara.memenator.MyOnClickListener;
import co.mantara.memenator.R;
import co.mantara.memenator.model.Meme;
import co.mantara.memenator.util.Constanta;

/**
 * Created by Gilang on 04/09/2016.
 * gilangmantara@gmail.com
 */
public class MemeRecyclerAdapter extends RecyclerView.Adapter<MemeRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Meme> listMeme;
    private MyOnClickListener clickListener;

    public MemeRecyclerAdapter(Context context, List<Meme> listMeme) {
        this.context = context;
        this.listMeme = listMeme;
    }

    @Override
    public MemeRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_meme, viewGroup, false);
        return new ViewHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(final MemeRecyclerAdapter.ViewHolder viewHolder, final int i) {
        final Meme meme = listMeme.get(i);

        // Display MEME Thumbnail
        final String PATH = meme.getPath();
        Picasso.with(context).load("file://" + PATH).into(viewHolder.ivMeme);

        final File fileMeme = new File(Environment.getExternalStorageDirectory()
                + File.separator + Constanta.FilePath.GENERATED,
                listMeme.get(i).getName());

        // Listener button Delete item
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean memeDeleted = fileMeme.delete();
                File file = new File(PATH);
                boolean thumbDeleted = file.delete();
                if (memeDeleted && thumbDeleted) {
                    listMeme.remove(i);
                    notifyDataSetChanged();
                }
            }
        });

        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(PATH);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        // image/jpeg or image/png or image/webp
        final String finalType = type;
        final Bitmap.CompressFormat finalBitmapCompress = getBitmapCompress(finalType);

        // Listener button Share item
        viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap meme = BitmapFactory.decodeFile(fileMeme.getPath());
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(finalType);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "title");
                values.put(MediaStore.Images.Media.MIME_TYPE, finalType);
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);

                OutputStream outstream;
                try {
                    outstream = context.getContentResolver().openOutputStream(uri);
                    meme.compress(finalBitmapCompress, 100, outstream);
                    outstream.close();
                } catch (Exception e) {
                    System.err.println(e.toString());
                }

                share.putExtra(Intent.EXTRA_STREAM, uri);
                context.startActivity(Intent.createChooser(share, "Share Image"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listMeme.size();
    }

    public void setOnItemClickListener(MyOnClickListener listener) {
        this.clickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected ImageView ivMeme;
        protected ImageButton btnDelete, btnShare;

        private MyOnClickListener clickListener;

        public ViewHolder(View itemView, MyOnClickListener clickListener) {
            super(itemView);
            ivMeme = (ImageView) itemView.findViewById(R.id.ivMeme);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);
            btnShare = (ImageButton) itemView.findViewById(R.id.btnShare);

            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
            itemView.setClickable(true);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private Bitmap.CompressFormat getBitmapCompress(String type) {
        switch (type) {
            case "image/jpeg":
                return Bitmap.CompressFormat.JPEG;
            case "image/png":
                return Bitmap.CompressFormat.PNG;
            case "image/webp":
                return Bitmap.CompressFormat.WEBP;
            default:
                return Bitmap.CompressFormat.JPEG;
        }
    }
}

