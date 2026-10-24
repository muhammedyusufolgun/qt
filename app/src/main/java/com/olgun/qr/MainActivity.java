package com.olgun.qr;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.net.Uri;import android.provider.MediaStore;import android.content.ContentValues;import android.view.*;import android.widget.*;import java.io.*;import com.google.zxing.*;import com.google.zxing.common.BitMatrix;import com.google.zxing.qrcode.QRCodeWriter;import com.google.zxing.EncodeHintType;import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;import java.util.*;
public class MainActivity extends Activity {
 EditText input;ImageView preview;Spinner sizes;Bitmap qr,logo;TextView status;
 int[] px={256,512,1024,2048};int PICK=12;int buttonIndex=0;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setBackgroundColor(Color.rgb(247,248,250));LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(22),dp(30),dp(22),dp(40));scroll.addView(root);setContentView(scroll);getWindow().getDecorView().setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());scroll.setPadding(0,bars.top,0,bars.bottom);return insets;});
 
 
 heading(root,"01   İÇERİK");input=new EditText(this);input.setHint("https://ornek.com veya metninizi yazın...");input.setMinLines(3);input.setGravity(Gravity.TOP);input.setTextColor(Color.BLACK);input.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.DKGRAY));root.addView(input);input.setText(getPreferences(0).getString("text",""));
 button(root,"QR KODU OLUŞTUR",()->generate(false));
 heading(root,"02   LOGO EKLE");button(root,"+ LOGO SEÇ",()->{Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,PICK);});button(root,"LOGOYU KALDIR",()->{logo=null;message("Logo kaldırıldı");});
 heading(root,"03   ÖNİZLEME & DIŞA AKTAR");preview=new ImageView(this);preview.setBackgroundColor(Color.WHITE);preview.setAdjustViewBounds(true);preview.setScaleType(ImageView.ScaleType.FIT_CENTER);preview.setVisibility(View.GONE);root.addView(preview,new LinearLayout.LayoutParams(-1,0));
 sizes=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"256 × 256","512 × 512","1024 × 1024","2048 × 2048"});sizes.setAdapter(adapter);sizes.setSelection(1);root.addView(sizes);
 button(root,"↓ PNG OLARAK KAYDET",()->generate(true));button(root,"METNİ KOPYALA",()->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("QR",input.getText().toString()));message("Metin kopyalandı");});
 button(root,"YENİDEN BAŞLA",()->{input.setText("");preview.setImageDrawable(null);preview.setVisibility(View.GONE);preview.setLayoutParams(new LinearLayout.LayoutParams(-1,0));getPreferences(0).edit().remove("text").apply();});
 status=new TextView(this);status.setTextColor(Color.DKGRAY);status.setPadding(0,dp(16),0,dp(10));root.addView(status);
 TextView footer=new TextView(this);footer.setText("Muhammed Yusf OLGUN");footer.setTextSize(12);footer.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.ITALIC));footer.setTextColor(Color.rgb(95,103,114));footer.setGravity(Gravity.CENTER);footer.setPadding(0,dp(24),0,dp(8));root.addView(footer,new LinearLayout.LayoutParams(-1,-2));
 
 }
 void heading(LinearLayout r,String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(12);t.setTypeface(null,1);t.setTextColor(Color.BLACK);t.setPadding(0,dp(24),0,dp(10));r.addView(t);}
 int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 void button(LinearLayout r,String label,Runnable action){
 TextView b=new TextView(this);
 b.setText(label);b.setTextSize(13);b.setTypeface(null,1);
 b.setGravity(Gravity.CENTER);
 int[] colors={Color.rgb(187,247,208),Color.rgb(254,249,195),Color.rgb(254,202,202),Color.rgb(17,24,39),Color.rgb(229,231,235),Color.rgb(55,65,81)};
 int color=colors[Math.min(buttonIndex++,colors.length-1)];
 b.setTextColor((buttonIndex==4||buttonIndex==6)?Color.WHITE:Color.rgb(17,24,39));
 android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
 bg.setColor(color);bg.setCornerRadius(dp(12));
 b.setBackground(bg);b.setClickable(true);b.setFocusable(true);
 b.setPadding(dp(12),0,dp(12),0);
 LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));
 lp.setMargins(0,dp(5),0,dp(5));r.addView(b,lp);b.setOnClickListener(v->action.run());
 }
 void message(String s){status.setText(s);}
 void generate(boolean save){String s=input.getText().toString();if(s.trim().isEmpty()){message("Metin veya URL giriniz");return;}getPreferences(0).edit().putString("text",s).apply();try{int n=px[sizes.getSelectedItemPosition()];Map<EncodeHintType,Object> opts=new HashMap<>();opts.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);opts.put(EncodeHintType.MARGIN,2);BitMatrix m=new QRCodeWriter().encode(s,BarcodeFormat.QR_CODE,n,n,opts);qr=Bitmap.createBitmap(n,n,Bitmap.Config.ARGB_8888);for(int y=0;y<n;y++)for(int x=0;x<n;x++)qr.setPixel(x,y,m.get(x,y)?Color.BLACK:Color.WHITE);if(logo!=null){Canvas c=new Canvas(qr);int z=n/5;Bitmap l=Bitmap.createScaledBitmap(logo,z,z,true);Paint p=new Paint(3);c.drawRect((n-z)/2-6,(n-z)/2-6,(n+z)/2+6,(n+z)/2+6,white());c.drawBitmap(l,(n-z)/2,(n-z)/2,p);}preview.setVisibility(View.VISIBLE);preview.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(340)));preview.setImageBitmap(qr);preview.requestLayout();if(save){ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"QR_"+System.currentTimeMillis()+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/QR");Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(u==null)throw new IOException("Dosya açılamadı");try(OutputStream out=getContentResolver().openOutputStream(u)){if(!qr.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("PNG yazılamadı");}message("PNG kaydedildi: Resimler/QR");}else message("QR kod oluşturuldu");}catch(Exception e){message("Hata: "+e.getMessage());}}
 Paint white(){Paint p=new Paint();p.setColor(Color.WHITE);return p;}
 protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==PICK&&result==RESULT_OK&&data!=null){try(InputStream in=getContentResolver().openInputStream(data.getData())){logo=BitmapFactory.decodeStream(in);message("Logo seçildi");}catch(Exception e){message("Logo açılamadı");}}}
}