package com.silageproerp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.silageproerp.ui.dashboard.DashboardFragment;
import com.silageproerp.ui.farmers.FarmersFragment;
import com.silageproerp.ui.lands.LandsFragment;
import com.silageproerp.ui.harvest.HarvestFragment;
import com.silageproerp.ui.inventory.InventoryFragment;
import com.silageproerp.ui.buyers.BuyersFragment;
import com.silageproerp.ui.sellers.SellersFragment;
import com.silageproerp.ui.sales.SalesFragment;
import com.silageproerp.ui.costing.CostingFragment;
import com.silageproerp.ui.cattle.CattleFragment;
import com.silageproerp.ui.fattening.FatteningFragment;
import com.silageproerp.ui.packaging.PackagingFragment;
import com.silageproerp.ui.reports.ReportsFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            fragment = new DashboardFragment();
            setTitle("Dashboard");
        } else if (id == R.id.nav_farmers) {
            fragment = new FarmersFragment();
            setTitle("Farmers");
        } else if (id == R.id.nav_lands) {
            fragment = new LandsFragment();
            setTitle("Lands & Fields");
        } else if (id == R.id.nav_harvest) {
            fragment = new HarvestFragment();
            setTitle("Harvest & Yield");
        } else if (id == R.id.nav_packaging) {
            fragment = new PackagingFragment();
            setTitle("Packaging");
        } else if (id == R.id.nav_inventory) {
            fragment = new InventoryFragment();
            setTitle("Inventory");
        } else if (id == R.id.nav_buyers) {
            fragment = new BuyersFragment();
            setTitle("Buyers");
        } else if (id == R.id.nav_sellers) {
            fragment = new SellersFragment();
            setTitle("Suppliers / Sellers");
        } else if (id == R.id.nav_sales) {
            fragment = new SalesFragment();
            setTitle("Sales & Delivery");
        } else if (id == R.id.nav_costing) {
            fragment = new CostingFragment();
            setTitle("Costing & Expenses");
        } else if (id == R.id.nav_cattle) {
            fragment = new CattleFragment();
            setTitle("Cattle");
        } else if (id == R.id.nav_fattening) {
            fragment = new FatteningFragment();
            setTitle("Fattening Program");
        } else if (id == R.id.nav_reports) {
            fragment = new ReportsFragment();
            setTitle("Reports & History");
        }

        if (fragment != null) {
            loadFragment(fragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
