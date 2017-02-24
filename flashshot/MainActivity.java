/* ȡ�ԡ�Android����Ӧ��ʵս��⡷p438
 * ���ȣ��洢��Ƭʱ�׳�exception����ʱɾ����Ƭ�洢����
 * */
package com.example.selfie;

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
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SdCardPath")
public class MainActivity extends Activity
  implements SurfaceHolder.Callback{
	final private static String STILL_IMAGE_FILE = "capture.jpg";
	private String strCaptureFilePath = "/sdcard/camera_snap.jpg";
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
    BroadcastReceiver mReceiver;
    public Timer timer = new Timer();
    private ImageView view;
    public static boolean mtest=false;
    public int booleantakepicture=-1;
    AudioManager meng;
    int screenWidth;
    int screenHeight;
    int volume ;
     Intent intent;
    MediaPlayer shootMP;
    Bitmap cbitmap,bMapRotate;
    File file;
    BroadcastReceiver mReceiverDestroyl ;
    private List<Bitmap> mBitmap = new ArrayList<Bitmap>();;
    //ע��㲥����MessageManager�Ǳ߽������ݽ������մ����Ĵ���
    private final String ACTION_NAME = "���͹㲥";
    private OnCameraStatusListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i("onCreate", "onCreate");
        super.onCreate(savedInstanceState);
        /* ʹӦ�ó���ȫ��Ļ���У���ʹ��title bar */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.flashshot);
        /* ȡ����Ļ�������� */
        getDisplayMetrics();
        findViews();
		getSurfaceHolder();
		mReceiverDestroyl=new BroadcastReceiver(){
		    public void onReceive(Context context, Intent intent) {
		    	System.out.println("���յ�finish��Ϣ����   Camera2");
		    	MainActivity.this.finish();
		    }
		};
		IntentFilter intentFilter = new IntentFilter("android.intent.action.Fakecall.Camera2");
		MainActivity.this.registerReceiver(mReceiverDestroyl, intentFilter);
    }
    @Override
    protected void onResume() {
    	Log.i("onResume", "onResume");
    	try {
    		initCamera();
    	} catch(IOException e ) {
    	}
    	super.onResume();
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	finish();
    }
    @Override
    protected void onStop() {
    	super.onStop();
    	finish();
    }
    @Override
    protected void onDestroy() {
      unregisterReceiver(mReceiverDestroyl);
     	resetCamera();
        if(cbitmap !=null){
          cbitmap.recycle();
          cbitmap=null;
        }if(bMapRotate !=null){
        bMapRotate.recycle();
        bMapRotate=null;
      }
     	 if (shootMP != null)
             shootMP.release();
     	shootMP=null;

    super.onDestroy();
    }

/* function:
 * ��previewʱ��ʵ����Camera,��ʼpreview
 * ��previewʱand�����ʱ��������һ��preview
 * previewʱ��������
 */
    private void initCamera() throws IOException
    {
    	Log.i("initCamera", "initCamera");
      if(!bPreviewing)
      {
        /* ���������Ԥ��ģʽ�������� */
        camera = Camera.open(0);
        camera.setDisplayOrientation(90);
      }
      //��Ԥ��ʱand�����ʱ������preview
      if (camera != null && !bPreviewing)
      {
        /* ����Camera.Parameters���� */
        Camera.Parameters parameters = camera.getParameters();
        /* ������Ƭ��ʽΪJPEG */
   //     parameters.setPictureFormat(PixelFormat.JPEG);
        /* ָ��preview����Ļ��С */
 //       parameters.setPreviewSize(intScreenWidth, intScreenHeight);
        /* ����ͼƬ�ֱ��ʴ�С */
 //     parameters.setPictureSize(intScreenWidth, intScreenHeight);
        /* ��Camera.Parameters������Camera */
        List<Size> pszize = parameters.getSupportedPreviewSizes();
		 if (null != pszize && 0 < pszize.size()) {
           int height1[] = new int[pszize.size()];// ����һ������
           Map<Integer, Integer> map = new HashMap<Integer, Integer>();
           for (int i = 0; i < pszize.size(); i++) {
                   Size size = (Size) pszize.get(i);
                   int sizeheight = size.height;
                   int sizewidth = size.width;
                   height1[i] = sizeheight;
                   map.put(sizeheight, sizewidth);
                   Log.d("  2", "size.width:"+sizewidth+"\tsize.height:"+sizeheight);
           }
           Arrays.sort(height1);
           List<Camera.Size > supportedPictureSizesList = parameters
                   .getSupportedPictureSizes();
           parameters.setPreviewSize(map.get(height1[height1.length-1]),height1[height1.length-1]);
		 }
        camera.setParameters(parameters);
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
    	Log.i("resetCamera", "resetCamera");
      if (camera != null && bPreviewing)
      {
    	  mSurfaceHolder.removeCallback(this);
    	  camera.setPreviewCallback(null);
    	  camera.stopPreview();
    	  camera.lock();
    	  camera.release();
    	  camera = null;
        /* �ͷ�Camera���� */
        bPreviewing = false;
      }
    }
/* func:ֹͣpreview*/
    private void takeAPicture() {
    	Log.i("takeAPicture", "takeAPicture");
        if (camera != null && bPreviewing && safeToTakePicture )
        {
          if(android.os.Build.MANUFACTURER.equals("Xiaomi")){
            camera.takePicture(null, null, jpegCallback);
            meng = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
            volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
            if (volume != 0){
              if (shootMP == null){
                shootMP = MediaPlayer.create(MainActivity.this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
              }if (shootMP != null){
                shootMP.start();
              }
              }
          }else{
//            camera.autoFocus(new AutoFocusCallback() {
//              @Override
//              public void onAutoFocus(final boolean success, final Camera camera) {
//                if (null != listener) {
//                  TimerTask task = new TimerTask(){
//                    @Override
//                    public void run() {
//                      listener.onAutoFocus(success);
//                    }
//                  };
//                  timer.schedule(task, 1550);
//                }
//                if (success) {
                  camera.takePicture(null, null, jpegCallback);
                  meng = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
                  volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);
                  if (volume != 0){
                    if (shootMP == null){
                      shootMP = MediaPlayer.create(MainActivity.this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                    }
                    if (shootMP != null){
                      shootMP.start();
                    }
                  }
                }
//              }
//            });
            safeToTakePicture=false;
//          }
        }
    }
    public void setOnCameraStatusListener(OnCameraStatusListener listener) {
        this.listener = listener;
    }
    public interface OnCameraStatusListener {

        // ������ս����¼�
        void onCameraStopped(byte[] data);

        // ����ʱ�Զ��Խ��¼�
        void onAutoFocus(boolean success);

        // ������Ļ�Խ��¼�
        void onTouchFocus(Camera mCamera);
    }
    /**
/*func:��ȡ��Ļ�ֱ���*/
    private void getDisplayMetrics() {
    	Log.i("getDisplayMetrics", "getDisplayMetrics");

    	DisplayMetrics dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    intScreenWidth = dm.widthPixels;
    intScreenHeight = dm.heightPixels;
    }

    private ShutterCallback shutterCallback = new ShutterCallback()
    {
      public void onShutter()
      {     // Shutter has closed
    	  Log.i("shutterCallback", "shutterCallback");
      }
    };

    private Camera.PictureCallback rawCallback = new Camera.PictureCallback()
    {
     @Override
      public void onPictureTaken(byte[] data, Camera camera)
      { Log.i("rawCallback", "rawCallback");
      }
    };

  private PictureCallback jpegCallback = new PictureCallback() {
    public void onPictureTaken(byte[] data, Camera _camera)
    {

      Log.i("jpegCallback", "jpegCallback");
      //	  BitmapFactory.Options opts=new BitmapFactory.Options();//��ȡ����ͼ��ʾ����Ļ��
      String sdCard = Environment.getExternalStorageDirectory().getPath();

      String dirFilePath = sdCard + File.separator + "DCIM" + File.separator + "Camera" + File.separator;
//    	  Bitmap bitmap = BitmapFactory.decodeByteArray( data , 0, data.length);
      cbitmap=BitmapFactory.decodeByteArray( data , 0, data.length);
      Configuration config = getResources().getConfiguration();
      if (config.orientation==1){ // ����
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(90);
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
          Log.v( "TGA" , "�����ļ����" );
        }
        else
        {
          Log.v( "TAG" , "�ļ����ڣ����贴��" );
        }
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd_HHmmss" );//���Է�����޸����ڸ�ʽ
        String time_stringFormat = dateFormat.format(date);
        String fileTemp="IMG_" + time_stringFormat+".jpg";
        System.out.println(time_stringFormat);
        System.out.println(dirFilePath);


        try {
          file = createFile( "IMG_" + time_stringFormat, ".jpg" , dirFile );
          BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( file ) );
          cbitmap.compress(Bitmap.CompressFormat.JPEG ,100, bos);
          bos.flush();
          bos.close();
          Log.i(" bos.clos", " bos.clos");

        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        // ��ΰ��ļ����뵽ϵͳͼ��

        try {
          MediaStore.Images.Media.insertImage(getContentResolver(),
            file.getAbsolutePath(), fileTemp, null);
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        // ���֪ͨͼ�����
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + dirFilePath+fileTemp)));
      }finally{
      }
    }
  };
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
    	Log.i("addSurfaceVLayout", "addSurLayout");
    	mFrameLayout01.addView(mSurfaceView01);
    }
/* func:�������Ԫ��Button capture,FrameLayout frame*/
    private void findViews() {
        /* ��SurfaceView��Ϊ���Preview֮�� */
        mSurfaceView01 = (SurfaceView) findViewById(R.id.mSurfaceView01);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.i("surfaceCreated", "surfaceCreated");
    	if(!bPreviewing && camera==null ) {
    		camera = Camera.open(0);
    		camera.setDisplayOrientation(90);
    	}
    }
    @Override
	public void surfaceChanged(SurfaceHolder holder,int format,int width,int height) {
//        Camera.Parameters mParameters = mCamera.getParameters();
 //       mParameters.setPictureSize(screenWidth, screenHeight);
 //       mCamera.setParameters(mParameters);
    	Log.i("surfaceChanged", "surfaceChanged");
		if(bPreviewing) {
			camera.stopPreview();
		}
		 WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//��ȡ���ڵĹ�����
	     Display display = wm.getDefaultDisplay();//��ô����������Ļ
		Camera.Parameters params = camera.getParameters();
		 List<Size> pszize = params.getSupportedPreviewSizes();
		 if (null != pszize && 0 < pszize.size()) {
            int height1[] = new int[pszize.size()];// ����һ������
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (int i = 0; i < pszize.size(); i++) {
                    Size size = (Size) pszize.get(i);
                    int sizeheight = size.height;
                    int sizewidth = size.width;
                    height1[i] = sizeheight;
                    map.put(sizeheight, sizewidth);
                    Log.d("  2", "size.width:"+sizewidth+"\tsize.height:"+sizeheight);
            }
            Arrays.sort(height1);
       //���ܻ����bug,�еĻ���û�в��Թ�
//       if(android.os.Build.MANUFACTURER.equals("Xiaomi") || android.os.Build.MANUFACTURER.equals("HUAWEI")){
       String a=android.os.Build.MODEL;
       Log.i("TAG469"," a "+a);
       //��Ϊ��ҫV8�������÷ֱ���.
       if(a.equals("KNT-UL10") || a.equals("H60-L11") ){
          Log.i("TAG469","android.os.Build.VERSION.RELEASE");
       }else{
         params.setPictureSize(map.get(height1[height1.length-1]), height1[height1.length-1]);
       }
//       }
//            List<Camera.Size > supportedPictureSizesList = params
//                    .getSupportedPictureSizes();
            //           params.setPreviewSize(map.get(height1[height1.length-1]),height1[height1.length-1]);
//          params.setPictureSize(map.get(height1[height1.length-3]), height1[height1.length-3]);
//           params.setPictureSize(1280, 960);
//           params.setPreviewSize(map.get(height1[height1.length-1]), height1[height1.length-1]);

//            int supportedPictureWidth = 0;
//            int supportedPictureHeight = 0;
//            for (int i = 0; i < supportedPictureSizesList.size() - 1; i++) {
//                supportedPictureWidth = supportedPictureSizesList.get(i).width;
//                supportedPictureHeight = supportedPictureSizesList.get(i).height;
//                int nextWidth = supportedPictureSizesList.get(i + 1).width;
//                int nextHeight = supportedPictureSizesList.get(i + 1).height;
//                if (supportedPictureWidth < nextWidth) {
//                    supportedPictureWidth = nextWidth;
//                }
//                if (supportedPictureHeight < nextHeight) {
//                    supportedPictureHeight = nextHeight;
//                }
//            }
//            supportedPictureWidth = supportedPictureWidth>supportedPictureSizesList.get(0).width ?supportedPictureWidth:supportedPictureSizesList.get(0).width;
//            supportedPictureHeight = supportedPictureHeight>supportedPictureSizesList.get(0).height ?supportedPictureHeight:supportedPictureSizesList.get(0).height;
////            params.setPictureSize((supportedPictureWidth/2), (supportedPictureHeight/2));
//            System.out.println(supportedPictureWidth+"  ..   "+supportedPictureHeight);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            params.setJpegQuality(100);
            System.out.println(map.get(height1[height1.length-3])  + "  hua  "+ height1[height1.length-3]);
            System.out.println(map.get(height1[height1.length-1])  + "  hua   "+ height1[height1.length-1]);
            System.out.println(map.get(height1[height1.length-2])  + "  hua  "+ height1[height1.length-2]);
            System.out.println(map.get(height1[height1.length-4])  + "  hua   "+ height1[height1.length-4]);
    }
		camera.cancelAutoFocus();//�Զ��Խ���
		camera.setParameters(params);
		try {
			camera.setPreviewDisplay(mSurfaceHolder);
		} catch(IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
		safeToTakePicture = true;
		bPreviewing = true;
		TimerTask task = new TimerTask(){
			@Override
			public void run() {
					takeAPicture();

			}
		};
			timer.schedule(task, 1550);

				TimerTask task2 = new TimerTask(){
					@Override
					public void run() {
						finish();
					}
				};
			timer.schedule(task2, 2550);
			}
    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {

    	if(camera != null &&  bPreviewing){
    		resetCamera();
    	Log.i("surfaceDestroyed", "surfaceDestroyed");
	   }
    }
}
