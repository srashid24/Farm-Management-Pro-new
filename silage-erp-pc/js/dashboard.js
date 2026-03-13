const Dashboard = {
  async render() {
    UI.title('Dashboard');
    const [farmers, lands, harvests, sales, costs, cattle, fattening, inventory] =
      await Promise.all([
        db.getAll('farmers'), db.getAll('lands'), db.getAll('harvests'),
        db.getAll('sales'), db.getAll('costs'), db.getAll('cattle'),
        db.getAll('fattening'), db.getAll('inventory'),
      ]);

    const totalYield     = harvests.reduce((s,r)=>s+(r.yieldTons||0),0);
    const totalBales     = harvests.reduce((s,r)=>s+(r.baleCount||0),0);
    const totalHa        = lands.reduce((s,r)=>s+(r.sizeHectares||0),0);
    const totalAc        = lands.reduce((s,r)=>s+(r.sizeAcres||0),0);
    const totalRevenue   = sales.reduce((s,r)=>s+(r.totalAmount||0),0);
    const totalOutstanding = sales.reduce((s,r)=>s+(r.balance||0),0);
    const totalCosts     = costs.reduce((s,r)=>s+(r.amount||0),0);
    const netProfit      = totalRevenue - totalCosts;
    const totalInvValue  = inventory.reduce((s,r)=>s+(r.totalValue||0),0);
    const activeCattle   = cattle.filter(c=>c.status==='Active').length;
    const activeFat      = fattening.filter(f=>f.status==='Active').length;
    const lowStock       = inventory.filter(i=>i.quantity<=i.minimumStock && i.minimumStock>0);
    const pendingDeliveries = sales.filter(s=>s.deliveryStatus==='Pending').length;
    const unpaidSales    = sales.filter(s=>s.paymentStatus==='Unpaid').length;

    UI.render(`
      ${lowStock.length ? `<div class="alert-low-stock" style="display:block">⚠️ LOW STOCK: ${lowStock.map(i=>i.itemName).join(', ')}</div>` : ''}

      <div style="margin-bottom:8px;font-size:13px;color:#888">
        Last updated: ${new Date().toLocaleString()}
      </div>

      <div class="card-title" style="margin-bottom:8px">FARM OVERVIEW</div>
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Farmers</div><div class="kpi-value">${farmers.length}</div></div>
        <div class="kpi green"><div class="kpi-label">Fields</div><div class="kpi-value">${lands.length}</div></div>
        <div class="kpi green"><div class="kpi-label">Total Land</div><div class="kpi-value">${UI.n(totalAc,0)} ac</div></div>
        <div class="kpi green"><div class="kpi-label">Hectares</div><div class="kpi-value">${UI.n(totalHa,1)} ha</div></div>
      </div>

      <div class="card-title" style="margin-bottom:8px;margin-top:8px">SILAGE PRODUCTION</div>
      <div class="kpi-grid">
        <div class="kpi brown"><div class="kpi-label">Total Yield</div><div class="kpi-value">${UI.n(totalYield,1)} t</div></div>
        <div class="kpi brown"><div class="kpi-label">Total Bales</div><div class="kpi-value">${totalBales}</div></div>
        <div class="kpi brown"><div class="kpi-label">Harvests</div><div class="kpi-value">${harvests.length}</div></div>
      </div>

      <div class="card-title" style="margin-bottom:8px;margin-top:8px">FINANCIALS</div>
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Revenue</div><div class="kpi-value">${UI.money(totalRevenue)}</div></div>
        <div class="kpi red"><div class="kpi-label">Total Costs</div><div class="kpi-value">${UI.money(totalCosts)}</div></div>
        <div class="kpi ${netProfit>=0?'green':'red'}"><div class="kpi-label">Net Profit</div><div class="kpi-value">${UI.money(netProfit)}</div></div>
        <div class="kpi ${totalOutstanding>0?'red':'green'}"><div class="kpi-label">Outstanding</div><div class="kpi-value">${UI.money(totalOutstanding)}</div></div>
        <div class="kpi"><div class="kpi-label">Inventory Value</div><div class="kpi-value">${UI.money(totalInvValue)}</div></div>
      </div>

      <div class="card-title" style="margin-bottom:8px;margin-top:8px">CATTLE & LIVESTOCK</div>
      <div class="kpi-grid">
        <div class="kpi blue"><div class="kpi-label">Active Cattle</div><div class="kpi-value">${activeCattle}</div></div>
        <div class="kpi blue"><div class="kpi-label">In Fattening</div><div class="kpi-value">${activeFat}</div></div>
      </div>

      <div class="card-title" style="margin-bottom:8px;margin-top:8px">ALERTS</div>
      <div class="kpi-grid">
        ${pendingDeliveries>0?`<div class="kpi red"><div class="kpi-label">Pending Deliveries</div><div class="kpi-value">${pendingDeliveries}</div></div>`:''}
        ${unpaidSales>0?`<div class="kpi red"><div class="kpi-label">Unpaid Invoices</div><div class="kpi-value">${unpaidSales}</div></div>`:''}
        ${lowStock.length>0?`<div class="kpi red"><div class="kpi-label">Low Stock Items</div><div class="kpi-value">${lowStock.length}</div></div>`:''}
        ${pendingDeliveries===0&&unpaidSales===0&&lowStock.length===0?`<div class="kpi green"><div class="kpi-label">All Good!</div><div class="kpi-value">✓</div></div>`:''}
      </div>`);
  },
};
