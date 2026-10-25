package com.olgun.qr;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.net.Uri;import android.provider.MediaStore;import android.content.ContentValues;import android.view.*;import android.widget.*;import java.io.*;import com.google.zxing.*;import com.google.zxing.common.BitMatrix;import com.google.zxing.qrcode.QRCodeWriter;import com.google.zxing.EncodeHintType;import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;import java.util.*;
public class MainActivity extends Activity {
 EditText input;ImageView preview;Spinner sizes;Bitmap qr,logo;TextView status;
 int[] px={256,512,1024,2048};int PICK=12;int PICK_QR=13;int buttonIndex=0;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setBackgroundColor(Color.rgb(247,248,250));LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(22),dp(30),dp(22),dp(40));scroll.addView(root);setContentView(scroll);getWindow().getDecorView().setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());scroll.setPadding(0,bars.top,0,bars.bottom);return insets;});
 
 
 heading(root,"01   İÇERİK");input=new EditText(this);input.setHint("Metin veya Url yazın");input.setMinLines(3);input.setGravity(Gravity.TOP);input.setTextColor(Color.BLACK);input.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.DKGRAY));root.addView(input);input.setText("");input.clearFocus();root.setFocusableInTouchMode(true);root.requestFocus();getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 button(root,"QR KODU OLUŞTUR",()->{hideKeyboard();generate(false);});
 heading(root,"02   LOGO EKLE");button(root,"+ LOGO SEÇ",()->{Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,PICK);});button(root,"LOGOYU KALDIR",()->{logo=null;if(!input.getText().toString().trim().isEmpty())generate(false);else message("Logo kaldırıldı");});
 heading(root,"03   ÖNİZLEME & DIŞA AKTAR");preview=new ImageView(this);preview.setBackgroundColor(Color.WHITE);preview.setAdjustViewBounds(true);preview.setScaleType(ImageView.ScaleType.FIT_CENTER);preview.setVisibility(View.GONE);root.addView(preview,new LinearLayout.LayoutParams(-1,0));
 sizes=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"256 × 256","512 × 512","1024 × 1024","2048 × 2048"});sizes.setAdapter(adapter);sizes.setSelection(1);root.addView(sizes);
 button(root,"↓ PNG OLARAK KAYDET",()->generate(true));button(root,"METNİ KOPYALA",()->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("QR",input.getText().toString()));message("Metin kopyalandı");});
 button(root,"Yeni QR Oluştur",()->{input.setText("");preview.setImageDrawable(null);preview.setVisibility(View.GONE);preview.setLayoutParams(new LinearLayout.LayoutParams(-1,0));getPreferences(0).edit().remove("text").apply();});
 button(root,"QR Barkod Seç/Oku",()->chooseQrImage());
 status=new TextView(this);status.setTextColor(Color.DKGRAY);status.setPadding(0,dp(16),0,dp(10));root.addView(status);
 TextView footer=new TextView(this);footer.setText("Muhammed Yusuf OLGUN");footer.setTextSize(12);footer.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.ITALIC));footer.setTextColor(Color.rgb(95,103,114));footer.setGravity(Gravity.CENTER);footer.setPadding(0,dp(24),0,dp(8));root.addView(footer,new LinearLayout.LayoutParams(-1,-2));
 
 }
 void heading(LinearLayout r,String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(12);t.setTypeface(null,1);t.setTextColor(Color.BLACK);t.setPadding(0,dp(24),0,dp(10));r.addView(t);}
 int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 void button(LinearLayout r,String label,Runnable action){
 TextView b=new TextView(this);
 b.setText(label);b.setTextSize(13);b.setTypeface(null,1);
 b.setGravity(Gravity.CENTER);
 int[] colors={Color.rgb(187,247,208),Color.rgb(254,249,195),Color.rgb(254,202,202),Color.rgb(17,24,39),Color.rgb(229,231,235),Color.rgb(229,231,235)};
 int color=colors[Math.min(buttonIndex++,colors.length-1)];
 b.setTextColor(buttonIndex==4?Color.WHITE:Color.rgb(17,24,39));
 android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
 bg.setColor(color);bg.setCornerRadius(dp(12));
 b.setBackground(bg);b.setClickable(true);b.setFocusable(true);
 b.setPadding(dp(12),0,dp(12),0);
 LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));
 lp.setMargins(0,dp(5),0,dp(5));r.addView(b,lp);b.setOnClickListener(v->action.run());
 }
 void chooseQrImage(){
 Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
 intent.addCategory(Intent.CATEGORY_OPENABLE);
 intent.setType("image/*");
 intent.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI,android.net.Uri.parse("content://com.android.externalstorage.documents/document/primary%3APictures%2FQR"));
 try{startActivityForResult(intent,PICK_QR);}catch(Exception e){message("Dosya seçici açılamadı: "+e.getMessage());}
 }
 void readQrImage(Uri uri){
 try(InputStream in=getContentResolver().openInputStream(uri)){
 if(in==null)throw new IOException("Dosya okunamadı");
 BitmapFactory.Options opt=new BitmapFactory.Options();opt.inJustDecodeBounds=true;
 try(InputStream bounds=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(bounds,null,opt);}
 opt.inJustDecodeBounds=false;opt.inSampleSize=1;
 while(Math.max(opt.outWidth/opt.inSampleSize,opt.outHeight/opt.inSampleSize)>1800)opt.inSampleSize*=2;
 Bitmap selected=BitmapFactory.decodeStream(in,null,opt);
 if(selected==null)throw new IOException("Görsel çözümlenemedi");
 int w=selected.getWidth(),h=selected.getHeight();
 int[] pixels=new int[w*h];selected.getPixels(pixels,0,w,0,0,w,h);
 com.google.zxing.RGBLuminanceSource source=new com.google.zxing.RGBLuminanceSource(w,h,pixels);
 com.google.zxing.BinaryBitmap binary=new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
 com.google.zxing.MultiFormatReader reader=new com.google.zxing.MultiFormatReader();
 Map<com.google.zxing.DecodeHintType,Object> hints=new HashMap<>();
 hints.put(com.google.zxing.DecodeHintType.TRY_HARDER,Boolean.TRUE);
 hints.put(com.google.zxing.DecodeHintType.POSSIBLE_FORMATS,java.util.Collections.singletonList(com.google.zxing.BarcodeFormat.QR_CODE));
 com.google.zxing.Result result;
 try{result=reader.decode(binary,hints);}
 catch(com.google.zxing.NotFoundException e){result=reader.decode(new com.google.zxing.BinaryBitmap(new com.google.zxing.common.GlobalHistogramBinarizer(source)),hints);}
 input.setText(result.getText());hideKeyboard();
 preview.setVisibility(View.VISIBLE);preview.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(340)));
 preview.setImageBitmap(selected);preview.requestLayout();
 message("QR barkod okundu. Metin içerik alanına aktarıldı.");
 }catch(com.google.zxing.NotFoundException e){message("Seçilen görselde okunabilir QR kod bulunamadı.");}
 catch(Exception e){message("QR okunamadı: "+e.getMessage());}
 }
 void hideKeyboard(){android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(imm!=null)imm.hideSoftInputFromWindow(input.getWindowToken(),0);input.clearFocus();}
 void message(String s){status.setText(s);}
 void generate(boolean save){String s=input.getText().toString();if(s.trim().isEmpty()){message("Metin veya URL giriniz");return;}getPreferences(0).edit().putString("text",s).apply();try{int n=px[sizes.getSelectedItemPosition()];Map<EncodeHintType,Object> opts=new HashMap<>();opts.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);opts.put(EncodeHintType.MARGIN,2);BitMatrix m=new QRCodeWriter().encode(s,BarcodeFormat.QR_CODE,n,n,opts);qr=Bitmap.createBitmap(n,n,Bitmap.Config.ARGB_8888);for(int y=0;y<n;y++)for(int x=0;x<n;x++)qr.setPixel(x,y,m.get(x,y)?Color.BLACK:Color.WHITE);if(logo!=null){Canvas c=new Canvas(qr);int z=n/5;Bitmap l=Bitmap.createScaledBitmap(logo,z,z,true);Paint p=new Paint(3);c.drawRect((n-z)/2-6,(n-z)/2-6,(n+z)/2+6,(n+z)/2+6,white());c.drawBitmap(l,(n-z)/2,(n-z)/2,p);}preview.setVisibility(View.VISIBLE);preview.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(340)));preview.setImageBitmap(qr);preview.requestLayout();if(save){ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"QR_"+System.currentTimeMillis()+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/QR");Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(u==null)throw new IOException("Dosya açılamadı");try(OutputStream out=getContentResolver().openOutputStream(u)){if(!qr.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("PNG yazılamadı");}message("PNG kaydedildi: Resimler/QR");vibrateOnce();openSavedPng(u);}else message("QR kod oluşturuldu");}catch(Exception e){message("Hata: "+e.getMessage());}}
 void vibrateOnce(){try{android.os.Vibrator vibrator=(android.os.Vibrator)getSystemService(VIBRATOR_SERVICE);if(vibrator!=null&&vibrator.hasVibrator()){vibrator.vibrate(android.os.VibrationEffect.createOneShot(120,android.os.VibrationEffect.DEFAULT_AMPLITUDE));}}catch(Exception ignored){}}
 void openSavedPng(Uri uri){try{Intent view=new Intent(Intent.ACTION_VIEW);view.setDataAndType(uri,"image/png");view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(view);}catch(android.content.ActivityNotFoundException e){message("PNG kaydedildi. Resimler/QR klasöründen açabilirsiniz.");}catch(Exception e){message("PNG kaydedildi; otomatik açılamadı: "+e.getMessage());}}
 Paint white(){Paint p=new Paint();p.setColor(Color.WHITE);return p;}
 protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==PICK_QR&&result==RESULT_OK&&data!=null){readQrImage(data.getData());return;}if(req==PICK&&result==RESULT_OK&&data!=null){try(InputStream in=getContentResolver().openInputStream(data.getData())){logo=BitmapFactory.decodeStream(in);if(logo==null)throw new IOException("Görsel okunamadı");if(!input.getText().toString().trim().isEmpty())generate(false);else message("Logo seçildi. QR oluşturmak için metin girin.");}catch(Exception e){message("Logo açılamadı");}}}
}