package com.example.water_logger;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class WelcomeActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "OperationSunrisePrefs";
    private static final String KEY_ACCEPTED = "accepted";
    private static final String KEY_LOCATION = "meeting_location";
    private static final String KEY_MUSIC = "music_on";
    private static final String[] CHECKLIST = {
            "Comfortable clothes", "Water bottle", "Phone", "Small snack",
            "Jacket/shawl if it's cold", "Comfortable shoes", "Your beautiful smile"
    };

    private SunriseSceneView sceneView;
    private LinearLayout content;
    private SharedPreferences prefs;
    private ProgressBar checklistProgress;
    private TextView missionStatus;
    private TextView countdownText;
    private final java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();
    private ValueAnimator skyAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        buildExperience();
        startCountdown();
    }

    private void buildExperience() {
        FrameLayout root = new FrameLayout(this);
        sceneView = new SunriseSceneView(this);
        root.addView(sceneView, new FrameLayout.LayoutParams(-1, -1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = dp(20);
        content.setPadding(pad, dp(36), pad, dp(60));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        addHero();
        addInvitation();
        addBoardingPass();
        addChecklist();
        addMystery();
        addMemories();
        addFinalIfAccepted();
    }

    private void addHero() {
        TextView music = pill("♫ Soft music: " + (prefs.getBoolean(KEY_MUSIC, false) ? "playing" : "paused"));
        music.setOnClickListener(v -> {
            boolean on = !prefs.getBoolean(KEY_MUSIC, false);
            prefs.edit().putBoolean(KEY_MUSIC, on).apply();
            music.setText("♫ Soft music: " + (on ? "playing" : "paused"));
        });
        content.addView(music);

        TextView intro = title("Sele... I have a little plan for tomorrow.", 30);
        intro.setAlpha(0f);
        intro.animate().alpha(1f).setDuration(2200).start();
        content.addView(intro);

        Button open = button("OPEN MY PLAN", "#FFD166", "#FF8FAB");
        open.setOnClickListener(v -> startSunrise(open));
        content.addView(open);

        TextView dawn = body("Tomorrow morning, before the world gets busy...\nI want to steal a little time with you.\n\n🌅 SUNRISE DATE", 21, true);
        dawn.setPadding(0, dp(28), 0, dp(12));
        content.addView(dawn);

        countdownText = body("", 16, false);
        content.addView(countdownText);
    }

    private void addInvitation() {
        MaterialCardView card = card();
        LinearLayout inner = stack(dp(22));
        inner.addView(title("Dear Sele,", 28));
        inner.addView(body("Tomorrow, I want to take you somewhere peaceful.\nNo stress. No arguments. No rushing.\nJust you, me, the morning sky, and some time together.\n\nWill you join me?", 18, false));
        LinearLayout row = stack(0); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER);
        Button yes = button("YES ❤️", "#FF8FAB", "#FFD166");
        Button maybe = button("Maybe...", "#FFFFFF", "#D7E3FC");
        yes.setOnClickListener(v -> acceptDate());
        row.addView(yes, new LinearLayout.LayoutParams(0, dp(56), 1));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, dp(56), 1); mp.setMargins(dp(10),0,0,0);
        row.addView(maybe, mp);
        inner.addView(row);
        card.addView(inner);
        content.addView(card);
    }

    private void addBoardingPass() {
        MaterialCardView pass = card();
        LinearLayout inner = stack(dp(18));
        inner.addView(title("SUNRISE DATE", 24));
        inner.addView(body("📅 Tomorrow\n⏰ 5:30 AM\n👕 Dress comfortably\n🎒 Bring only what you need", 18, false));
        EditText location = new EditText(this);
        location.setHint("📍 Your meeting location");
        location.setSingleLine(true);
        location.setText(prefs.getString(KEY_LOCATION, ""));
        location.setTextColor(Color.WHITE); location.setHintTextColor(0xCCFFFFFF);
        location.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) prefs.edit().putString(KEY_LOCATION, location.getText().toString()).apply(); });
        inner.addView(location);
        pass.addView(inner); content.addView(pass);
    }

    private void addChecklist() {
        MaterialCardView card = card(); LinearLayout inner = stack(dp(14));
        inner.addView(title("Your Mission: Get Ready 🌸", 24));
        checklistProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        checklistProgress.setMax(100); inner.addView(checklistProgress, new LinearLayout.LayoutParams(-1, dp(14)));
        missionStatus = body("0% ready", 17, true); inner.addView(missionStatus);
        for (int i=0;i<CHECKLIST.length;i++) {
            CheckBox cb = new CheckBox(this); cb.setText(CHECKLIST[i]); cb.setTextColor(Color.WHITE); cb.setTextSize(17); cb.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFFFFD166));
            cb.setChecked(prefs.getBoolean("check_"+i, false)); final int idx=i;
            cb.setOnCheckedChangeListener((b,c)-> { prefs.edit().putBoolean("check_"+idx, c).apply(); updateProgress(); });
            checkBoxes.add(cb); inner.addView(cb);
        }
        card.addView(inner); content.addView(card); updateProgress();
    }

    private void addMystery() { MaterialCardView card=card(); LinearLayout inner=stack(dp(14)); inner.setGravity(Gravity.CENTER); inner.addView(title("I have one little surprise...",24)); inner.addView(title("🔒",48)); inner.addView(button("DON'T OPEN YET", "#CDB4DB", "#FFC8DD")); inner.addView(body("You'll find out tomorrow.",16,false)); card.addView(inner); content.addView(card); }

    private void addMemories() {
        content.addView(title("A few memories before we make another one.", 24));
        HorizontalScrollView hsv = new HorizontalScrollView(this); LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        int[] imgs = {R.drawable.kal, R.drawable.bbb, R.drawable.sss, R.drawable.fb, R.drawable.water};
        for (int img: imgs) { ImageView photo = new ImageView(this); photo.setImageResource(img); photo.setScaleType(ImageView.ScaleType.CENTER_CROP); photo.setBackground(gradient("#FFFFFF", "#FFD6E0", dp(26))); photo.setClipToOutline(true); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(150), dp(120)); lp.setMargins(dp(6), dp(10), dp(6), dp(12)); row.addView(photo, lp); }
        hsv.addView(row); content.addView(hsv);
    }

    private void addFinalIfAccepted() { if (prefs.getBoolean(KEY_ACCEPTED, false)) addFinalScreen(); }
    private void acceptDate() { prefs.edit().putBoolean(KEY_ACCEPTED, true).apply(); sceneView.confetti(); addFinalScreen(); }
    private void addFinalScreen() { TextView finalText = title("It's a date. 🌅\n\nTomorrow, 5:30 AM.\nDon't be late, Sele.\n\nI'll be waiting.", 30); content.addView(finalText); }

    private void startSunrise(View v) { v.setEnabled(false); skyAnimator = ValueAnimator.ofFloat(sceneView.getProgress(), 1f); skyAnimator.setDuration(4500); skyAnimator.setInterpolator(new DecelerateInterpolator()); skyAnimator.addUpdateListener(a -> sceneView.setProgress((float)a.getAnimatedValue())); skyAnimator.start(); }
    private void startCountdown() { countdownText.postDelayed(new Runnable(){ public void run(){ Calendar target=Calendar.getInstance(); target.add(Calendar.DAY_OF_YEAR,1); target.set(Calendar.HOUR_OF_DAY,5); target.set(Calendar.MINUTE,30); target.set(Calendar.SECOND,0); long diff=Math.max(0,target.getTimeInMillis()-System.currentTimeMillis()); long h=diff/3600000, m=(diff/60000)%60, s=(diff/1000)%60; countdownText.setText(String.format(Locale.US,"Countdown to %s at 5:30 AM: %02dh %02dm %02ds", new SimpleDateFormat("EEE, MMM d", Locale.US).format(target.getTime()), h,m,s)); countdownText.postDelayed(this,1000);} }, 10); }
    private void updateProgress(){ int done=0; for(CheckBox c:checkBoxes) if(c.isChecked()) done++; int p=Math.round(done*100f/CHECKLIST.length); checklistProgress.setProgress(p); missionStatus.setText(p==100?"MISSION READY ❤️":p+"% ready"); }

    private MaterialCardView card(){ MaterialCardView c=new MaterialCardView(this); c.setRadius(dp(28)); c.setCardElevation(dp(10)); c.setCardBackgroundColor(0x33FFFFFF); c.setStrokeColor(0x55FFFFFF); c.setStrokeWidth(dp(1)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(18),0,dp(8)); c.setLayoutParams(lp); return c; }
    private LinearLayout stack(int p){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(p,p,p,p); return l; }
    private TextView title(String s,int sp){ TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(sp); t.setTypeface(Typeface.DEFAULT_BOLD); t.setGravity(Gravity.CENTER); t.setPadding(0,dp(10),0,dp(10)); return t; }
    private TextView body(String s,int sp,boolean center){ TextView t=new TextView(this); t.setText(s); t.setTextColor(0xEEFFFFFF); t.setTextSize(sp); t.setLineSpacing(dp(3),1); t.setGravity(center?Gravity.CENTER:Gravity.START); return t; }
    private TextView pill(String s){ TextView t=body(s,15,true); t.setPadding(dp(14),dp(8),dp(14),dp(8)); t.setBackground(gradient("#33FFFFFF","#22FFD166",dp(22))); return t; }
    private Button button(String s,String a,String b){ Button btn=new Button(this); btn.setText(s); btn.setTextColor(0xFF24162D); btn.setTypeface(Typeface.DEFAULT_BOLD); btn.setAllCaps(false); btn.setBackground(gradient(a,b,dp(28))); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.setMargins(0,dp(12),0,dp(8)); btn.setLayoutParams(lp); return btn; }
    private android.graphics.drawable.GradientDrawable gradient(String a,String b,int r){ android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.parseColor(a),Color.parseColor(b)}); g.setCornerRadius(r); return g; }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }

    public static class SunriseSceneView extends View { Paint p=new Paint(1); Random r=new Random(7); float progress=0f; float[] sx=new float[90], sy=new float[90], ss=new float[90]; boolean burst=false; public SunriseSceneView(android.content.Context c){ super(c); for(int i=0;i<sx.length;i++){sx[i]=r.nextFloat(); sy[i]=r.nextFloat()*.55f; ss[i]=1+r.nextFloat()*3;} }
        float getProgress(){return progress;} void setProgress(float f){progress=f; invalidate();} void confetti(){burst=true; invalidate(); postDelayed(()->{burst=false; invalidate();},3500);} @Override protected void onDraw(Canvas c){ int w=getWidth(), h=getHeight(); int top=blend(0xFF08112E,0xFFFF9A76,progress), bot=blend(0xFF1A235A,0xFFFFD166,progress); p.setShader(new LinearGradient(0,0,0,h,top,bot,Shader.TileMode.CLAMP)); c.drawRect(0,0,w,h,p); p.setShader(null); p.setColor(0xDDFFF7D6); c.drawCircle(w*.78f,h*(.22f-.1f*progress),45+90*progress,p); p.setColor(0xEEFFFFFF); for(int i=0;i<sx.length;i++){p.setAlpha((int)(255*(1-progress))); c.drawCircle(sx[i]*w,sy[i]*h,ss[i],p);} p.setAlpha(255); p.setColor(0x44FFFFFF); for(int i=0;i<4;i++) cloud(c, (i*.33f*w + progress*w*.18f)%(w+220)-110, h*(.18f+i*.08f), 55+i*8); p.setColor(0x66FF8FAB); for(int i=0;i<35;i++) c.drawCircle((i*73+progress*260)%w, (i*97+progress*420)%h, 3+(i%4), p); if(burst){p.setColor(0xFFFFD166); for(int i=0;i<80;i++) c.drawCircle((i*47)%w,(i*91)%h,4,p);} invalidate(); }
        void cloud(Canvas c,float x,float y,float s){ c.drawOval(x,y,x+s*2,y+s,p); c.drawOval(x+s*.5f,y-s*.35f,x+s*1.7f,y+s*.75f,p);} int blend(int a,int b,float f){ return Color.rgb((int)(Color.red(a)+(Color.red(b)-Color.red(a))*f),(int)(Color.green(a)+(Color.green(b)-Color.green(a))*f),(int)(Color.blue(a)+(Color.blue(b)-Color.blue(a))*f)); }}
}
