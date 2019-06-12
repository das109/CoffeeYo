package mobileApp.project.CoffeeYo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class User_Order extends Fragment {
    private OnFragmentInteractionListener mListener;
    private DatabaseReference mPostReference;
    private FirebaseAuth mAuth;
    private String cafe_name, take, menu, count, price = "";
    private ArrayList<menuitem> menu_list;
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private TextView TextCongestion;
    private RadioButton r_btn1, r_btn2;
    private RadioGroup radioGroup;
    private Button YesButton;


    public User_Order() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
//////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user__order, container, false);


        cafe_name = getArguments().getString("cafe_name");
        menu_list = new ArrayList<menuitem>();
        recyclerView = view.findViewById(R.id.cafemenulist);
        adapter = new MenuAdapter();

        TextCongestion = (TextView)view.findViewById(R.id.textViewcon);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setItemClick(new MenuAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {

                //클릭시 실행될 함수 작성
            }
        });

        mPostReference = FirebaseDatabase.getInstance().getReference();

        radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);
        r_btn1 = (RadioButton)view.findViewById(R.id.r_btn1);
        r_btn2 = (RadioButton)view.findViewById(R.id.r_btn2);

        YesButton = (Button) view.findViewById(R.id.Yesbutton);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                        if (r_btn2.isChecked() == true) {
                            take = "Take-out";
                        } else if (r_btn1.isChecked() == true) {
                            take = "For-here";

                        }
                    }
                });




        YesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment OrderCheckFragment = new OrderCheckFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("cafe_name", cafe_name);
                        String[] order = new String[10];
                        int i = menu_list.size();
                        int Num = 0;
                        int Sum = 0;
                        /*
                        for (int j=0; j < i; j++) {
                            menu = menu_list.get()[0];
                            count = ((TextView) recyclerView.findViewHolderForAdapterPosition(j).itemView.findViewById(R.id.count)).getText().toString();
                            price = menu_list[j][1];
                            if (Integer.parseInt(count) >= 1) {
                                order[Num]= menu+": "+count+"개";
                                Num++;
                                }
                            }
                            */
                        for(menuitem men : menu_list){
                            menu = men.getMenu();
                            count = men.getCount();
                            price = men.getPrice();
                            if (Integer.parseInt(count) >= 1) {
                                Sum += Integer.parseInt(price) * Integer.parseInt(count);
                                bundle.putString(menu, count);
                                order[Num]= menu;
                                Num++;
                            }

                        }
                        String su = String.valueOf(Sum);
                        bundle.putStringArray("order", order);
                        bundle.putString("Num", String.valueOf(Num));
                        bundle.putString("take", take);
                        bundle.putString("Sum",su);

                        OrderCheckFragment.setArguments(bundle);

                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.content_main, OrderCheckFragment);

                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();


                        cafe_name = "";
                        take = "";

            }
        });

        getFirebaseDatabase();
        return view;
    }

    //잠깐만 이거 불러오는 거랑 넣는 게 달라
    //불러오는 건 cafe 메뉴 정보인데 넣는 건 주문 내역이야
    public void getFirebaseDatabase() {

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("onDataChange", "Data is Updated");
                menu_list.clear();

                for(DataSnapshot postListener: dataSnapshot.child("menu").getChildren()) {

                    String key = postListener.getKey();
                    CafeMenudatabase get = postListener.getValue(CafeMenudatabase.class);
                    menu_list.add(new menuitem(get.menu_name, get.price, "0"));
                    Log.d("getFirebaseDatabase", "key: " + key);
                    Log.d("getFirebaseDatabase", "info: " + get.menu_name + get.price);
                }



                String congestion = dataSnapshot.child("congestion").getValue().toString();
                TextCongestion.setText("카페 밀도: "+congestion);


                adapter.setItems(menu_list);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mPostReference.child("cafe_list").child(cafe_name).addValueEventListener(postListener);
    }




    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
