/* ȡ�ԡ�Android����Ӧ��ʵս��⡷p438
 * ���ȣ��洢��Ƭʱ�׳�exception����ʱɾ����Ƭ�洢����
 * */
package com.example.selfie;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
@SuppressLint("SdCardPath")
public class MainActivity extends Activity
  implements SurfaceHolder.Callback{
	final private static String STILL_IMAGE_FILE = "capture.jpg";
	private String strCaptureFilePath = "/sdcard/camera_snap.jpg";
    private String TAG = "IsiCamera2Activity";//??
	private Button btn_capture;
    private Camera camera = null;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView01;
    private FrameLayout mFrameLayout01;
    private boolean bPreviewing =false;
    private int intScreenWidth;
    private int intScreenHeight;
    private File mFile = null;
    private boolean safeToTakePicture = false;
    BroadcastReceiver mReceiver,mReceiver2;
    private ImageView view;
    public static boolean mtest=false;
    AudioManager meng;
    int volume ;
  Intent intent;
    MediaPlayer shootMP;
    private List<Bitmap> mBitmap = new ArrayList<Bitmap>();;
    //ע��㲥����MessageManager�Ǳ߽������ݽ������մ����Ĵ���
    private final String ACTION_NAME = "���͹㲥";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* ʹӦ�ó���ȫ��Ļ���У���ʹ��title bar */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        /* ȡ����Ļ�������� */
        getDisplayMetrics();
        findViews();
		getSurfaceHolder();

        btn_capture.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				takeAPicture();
			}
        });

    mReceiver=new BroadcastReceiver(){
	    public void onReceive(Context context, Intent intent) {
	    	btn_capture.performClick();
	    }
	};
	IntentFilter intentFilter = new IntentFilter("android.intent.action.Camera");
	registerReceiver(mReceiver, intentFilter);

	mReceiver2=new BroadcastReceiver(){
	    public void onReceive(Context context, Intent intent) {
	    	System.out.println("���յ�finish��Ϣ����   IsiCamera2Activity");
	    	MainActivity.this.finish();
	    }
	};
	IntentFilter intentFilter2 = new IntentFilter("android.intent.action.Camerafinish");
	registerReceiver(mReceiver2, intentFilter2);
    }
	@Override
    public void onStop() {
		stopPreview();
    	resetCamera();
    	super.onStop();
    }
    @Override
    protected void onResume() {
    	try {
    		initCamera();
    	} catch(IOException e ) {
    		Log.e(TAG,"initCamera() in Resume() erorr!");
    	}
    	super.onResume();
    }

    @Override
    protected void onDestroy() {
    	unregisterReceiver(mReceiver);
    	unregisterReceiver(mReceiver2);
    	super.onDestroy();
    	stopPreview();
    	resetCamera();
    }
    @Override
    protected void onPause() {
    	stopPreview();
    	resetCamera();
    	finish();
    	super.onPause();
    }

/* function:
 * ��previewʱ��ʵ����Camera,��ʼpreview
 * ��previewʱand�����ʱ��������һ��preview
 * previewʱ��������
 */
    private void initCamera() throws IOException
    {
      if(!bPreviewing)
      {
        /* ���������Ԥ��ģʽ�������� */
        camera = Camera.open(1);
        camera.setDisplayOrientation(90);
      }
      //��Ԥ��ʱand�����ʱ������preview
      if (camera != null && !bPreviewing)
      {
        Log.i(TAG, "inside the camera");
        /* ����Camera.Parameters���� */
        Camera.Parameters parameters = camera.getParameters();
        /* ������Ƭ��ʽΪJPEG */
        parameters.setPictureFormat(PixelFormat.JPEG);
        /* ָ��preview����Ļ��С */
      //  parameters.setPreviewSize(intScreenWidth, intScreenHeight);
        /* ����ͼƬ�ֱ��ʴ�С */
      //  parameters.setPictureSize(intScreenWidth, intScreenHeight);
        /* ��Camera.Parameters������Camera */
   //     camera.setParameters(parameters);
        /* setPreviewDisplayΨһ�Ĳ���ΪSurfaceHolder */
        camera.setPreviewDisplay(mSurfaceHolder);
        /* ��������Preview */
        camera.startPreview();
        bPreviewing = true;
      }
    }
/* func:ֹͣpreview,�ͷ�Camera����*/
    private void resetCamera()
    {
      if (camera != null && bPreviewing)
      {
        camera.stopPreview();
        /* �ͷ�Camera���� */
        camera.release();
        camera = null;
        bPreviewing = false;
      }
    }
/* func:ֹͣpreview*/
    private void stopPreview() {
    	if (camera != null && bPreviewing) {
            Log.v(TAG, "stopPreview");
            camera.stopPreview();
        }
    }
    private void takeAPicture() {
        if (camera != null && bPreviewing && safeToTakePicture )
        {
          /* ����takePicture()�������� */
          camera.takePicture
          (null, null,jpegCallback);//����PictureCallback interface�Ķ�����Ϊ����
          safeToTakePicture=false;
        }
        meng = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
       volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
       if (volume != 0)
       {
           if (shootMP == null)
               shootMP = MediaPlayer.create(MainActivity.this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
           if (shootMP != null)
               shootMP.start();
       }


    }
/*func:��ȡ��Ļ�ֱ���*/
    private void getDisplayMetrics() {
    	DisplayMetrics dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    intScreenWidth = dm.widthPixels;
    intScreenHeight = dm.heightPixels;
    Log.i(TAG, Integer.toString(intScreenWidth));
    }

    private ShutterCallback shutterCallback = new ShutterCallback()
    {
      public void onShutter()
      {     // Shutter has closed
      }
    };

    private Camera.PictureCallback rawCallback = new Camera.PictureCallback()
    {
     @Override
      public void onPictureTaken(byte[] data, Camera camera)
      {
      }
    };

    private PictureCallback jpegCallback = new PictureCallback()
    {
      public void onPictureTaken(byte[] data, Camera _camera)
      {
    	  BitmapFactory.Options opts=new BitmapFactory.Options();//��ȡ����ͼ��ʾ����Ļ��
    	  String sdCard = Environment.getExternalStorageDirectory().getPath();
 //   	  String sdCard = "/storage/sdcard1";

    	  Log.i("IsiCamera2Activity", ""+sdCard);

    	  String dirFilePath = sdCard + File.separator + "DCIM" + File.separator + "Camera" + File.separator;

    	  Bitmap cbitmap=BitmapFactory.decodeFile(dirFilePath);

//    	  Bitmap bitmap = BitmapFactory.decodeByteArray( data , 0, data.length);
          cbitmap=BitmapFactory.decodeByteArray( data , 0, data.length);
          Bitmap bMapRotate;
          Configuration config = getResources().getConfiguration();
          if (config.orientation==1){ // ����
        	  Matrix matrix = new Matrix();
        	  matrix.reset();
        	  matrix.postRotate(270);
        	  bMapRotate = Bitmap.createBitmap(cbitmap, 0, 0,cbitmap.getWidth(),
        			  cbitmap.getHeight(),matrix, true);
        	  cbitmap = bMapRotate;

          }
    	  try
    	  {
    	  //���������Ƭ���ļ���
    	  File dirFile = new File( dirFilePath );
    	  if( !dirFile.exists() )
    	  {
    	  dirFile.mkdir();
    	  Log.v( TAG , "�����ļ����" );
    	  }
    	  else
    	  {
    		  Log.i("IsiCamera2Activity", ""+sdCard);
    	  Log.v( TAG , "�ļ����ڣ����贴��" );
    	  }
    	  Date date = new Date();
    	  SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd_HHmmss" );//���Է�����޸����ڸ�ʽ
    	  String time_stringFormat = dateFormat.format( date );
    	  String fileTemp="IMG_" + time_stringFormat+".jpg";
    	  File file = null;
		try {
			file = createFile( "IMG_" + time_stringFormat, ".jpg", dirFile );
			StringBuilder log = new StringBuilder();
	        String inPath = getInnerSDCardPath();
	        Log.i("����SD��·����" ,inPath + "\r\n");
	        String extSdcardPath = System.getenv("SECONDARY_STORAGE");
			Log.i("extSdcardPath   " ,""+extSdcardPath);

			Log.i("getInnerSDCardPath" ,""+getInnerSDCardPath());
	        List<String> extPaths = getExtSDCardPath();
	        for (String path : extPaths) {
	        	Log.i("����SD��·����" ,path + "\r\n");
	        }
			Log.i("IsiCamera2Activity", "file.getAbsolutePath()      "+file.getAbsolutePath());
			String inPath2 =Environment.getRootDirectory().getAbsoluteFile() + File.separator + "etc" + File.separator + "vold.fstab";
					Log.i("inPath2        ",inPath2);
			BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( file ) );
			cbitmap.compress(Bitmap.CompressFormat.JPEG ,100 , bos );
	    	  bos.flush();
	    	  bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ��ΰ��ļ����뵽ϵͳͼ��
//		MediaStore.Images.Media.insertImage(IsiCamera2Activity.this.getContentResolver(),
//				file.getAbsolutePath(), fileTemp, null);

		// ���֪ͨͼ�����
	    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + dirFilePath+fileTemp)));
//	    MediaScannerConnection.scanFile(IsiCamera2Activity.this, new String[]{file.toString()}, null, null);
	    safeToTakePicture = true;
		bPreviewing=true;
    	  resetCamera();
      }finally{
    	  try {
			initCamera();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      }
    };
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// �����뵱ǰ Java Ӧ�ó�����ص�����ʱ����
        BufferedInputStream in=null;
        BufferedReader inBr=null;
        try {
            Process p = run.exec(cmd);// ������һ��������ִ������
            in = new BufferedInputStream(p.getInputStream());
            inBr = new BufferedReader(new InputStreamReader(in));


            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // �������ִ�к��ڿ���̨�������Ϣ
            Log.i("CommonUtil:getSDCardPath", lineStr);
                if (lineStr.contains("sdcard")
                        && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure",
                                "");
                        return result;
                    }
                }
                // ��������Ƿ�ִ��ʧ�ܡ�
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0��ʾ����������1������������
                Log.e("CommonUtil:getSDCardPath", "����ִ��ʧ��!");
                }
            }
        } catch (Exception e) {
            Log.e("CommonUtil:getSDCardPath", e.toString());
            //return Environment.getExternalStorageDirectory().getPath();
        }finally{
            try {
            if(in!=null){
                    in.close();
            }
} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
            try {
            if(inBr!=null){
    inBr.close();
            }
} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
        }
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * ��ȡ����SD��·��
     * @return  Ӧ�þ�һ����¼���
     */
    public List<String> getExtSDCardPath()
    {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSd"))
                {
                    String [] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory())
                    {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return lResult;
    }

    public File  createFile(String prefix, String suffix, File directory)
            throws IOException {
        // Force a prefix null check first
        if (prefix.length() < 3) {
            throw new IllegalArgumentException("prefix must be at least 3 characters");
        }
        if (suffix == null) {
            suffix = ".tmp";
        }
        File tmpDirFile = directory;
        if (tmpDirFile == null) {
            String tmpDir = System.getProperty("java.io.tmpdir", ".");
            tmpDirFile = new File(tmpDir);
        }
        File result;
        do {
            result = new File(tmpDirFile, prefix + suffix);
        } while (!result.createNewFile());
        return result;
    }
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
                ExifInterface exifInterface = new ExifInterface(path);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return degree;
    }
/* get a fully initialized SurfaceHolder*/
    private void getSurfaceHolder() {
		mSurfaceHolder = mSurfaceView01.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
/* ��SurfaceView �ӵ�FrameLayout ��*/
    private void addSurfaceViewToFrameLayout() {
    	mFrameLayout01.addView(mSurfaceView01);
    }
/* func:�������Ԫ��Button capture,FrameLayout frame*/
    private void findViews() {
    	btn_capture = (Button) findViewById(R.id.btn_capture);
        /* ��SurfaceView��Ϊ���Preview֮�� */
        mSurfaceView01 = (SurfaceView) findViewById(R.id.mSurfaceView01);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	if(!bPreviewing) {
    		camera = Camera.open(1);
    		camera.setDisplayOrientation(90);
    	}
    }
    @Override
	public void surfaceChanged(SurfaceHolder holder,int format,int width,int height) {
		if(bPreviewing) {
			camera.stopPreview();
		}

		Camera.Parameters params = camera.getParameters();
//		 List<Size> pszize = params.getSupportedPreviewSizes();
//		 if (null != pszize && 0 < pszize.size()) {
//             int height1[] = new int[pszize.size()];// ����һ������
//             Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//             for (int i = 0; i < pszize.size(); i++) {
//                     Size size = (Size) pszize.get(i);
//                     int sizeheight = size.height;
//                     int sizewidth = size.width;
//                     height1[i] = sizeheight;
//                     map.put(sizeheight, sizewidth);
//                     Log.d(TAG, "size.width:"+sizewidth+"\tsize.height:"+sizeheight);
//             }
//             Arrays.sort(height1);
////          // ����
////             parameters.setPictureSize(map.get(height[0]),height[0]);
//             params.setPreviewSize(map.get(height1[height1.length-1]),height1[height1.length-1]);
//             System.out.println(map.get(height1[height1.length-1])  + "  "+ height1[height1.length-1]);
//     }
//		params.getSupportedPictureSizes();
//		params.setPreviewSize(640, 800);
//		camera.cancelAutoFocus();//�Զ��Խ���
	//	params.setPreviewSize(width, height);
		camera.setParameters(params);
		try {
			camera.setPreviewDisplay(mSurfaceHolder);
		} catch(IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
		safeToTakePicture = true;
		bPreviewing = true;
	}
    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
    	stopPreview();
		camera = null;
	}
	}
