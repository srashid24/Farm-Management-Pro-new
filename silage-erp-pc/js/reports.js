const Reports = {
  async render() {
    UI.title('Reports & History');
    const [farmers, lands, harvests, sales, costs, cattle, fattening, inventory, buyers, sellers, packaging] =
      await Promise.all([
        db.getAll('farmers'), db.getAll('lands'), db.getAll('harvests'),
        db.getAll('sales'), db.getAll('costs'), db.getAll('cattle'),
        db.getAll('fattening'), db.getAll('inventory'), db.getAll('buyers'),
        db.getAll('sellers'), db.getAll('packaging'),
      ]);

    const totalYield   = harvests.reduce((s,r)=>s+(r.yieldTons||0),0);
    const totalBales   = harvests.reduce((s,r)=>s+(r.baleCount||0),0);
    const totalHa      = lands.reduce((s,r)=>s+(r.sizeHectares||0),0);
    const totalAc      = lands.reduce((s,r)=>s+(r.sizeAcres||0),0);
    const totalRevenue = sales.reduce((s,r)=>s+(r.totalAmount||0),0);
    const totalTransport = sales.reduce((s,r)=>s+(r.transportCost||0),0);
    const totalOutstanding = sales.reduce((s,r)=>s+(r.balance||0),0);
    const totalCosts   = costs.reduce((s,r)=>s+(r.amount||0),0);
    const netProfit    = totalRevenue - totalCosts;
    const totalInvValue = inventory.reduce((s,r)=>s+(r.totalValue||0),0);
    const activeFattening = fattening.filter(f=>f.status==='Active').length;
    const activeCattle  = cattle.filter(c=>c.status==='Active').length;
    const totalFeedCost = fattening.reduce((s,r)=>s+(r.totalFeedCostToDate||0),0);

    // Cost by category
    const byCat = {};
    costs.forEach(r=>{ byCat[r.category]=(byCat[r.category]||0)+(r.amount||0); });
    const costBreakdown = Object.entries(byCat).sort((a,b)=>b[1]-a[1]).map(([cat,amt])=>`<div class="report-row"><span class="label">${cat}</span><span class="value text-red">${UI.money(amt)} <small>(${totalCosts>0?Math.round(amt/totalCosts*100):0}%)</small></span></div>`).join('');

    // Recent sales
    const recentSales = [...sales].sort((a,b)=>b.createdAt-a.createdAt).slice(0,5);
    const recentSalesHtml = recentSales.map(s=>`<div class="report-row"><span class="label">${s.buyerName} | ${s.saleDate||''}</span><span class="value">${UI.money(s.totalAmount)} ${UI.payBadge(s.paymentStatus)}</span></div>`).join('') || '<div style="color:#999;padding:8px 0">No sales yet</div>';

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Revenue</div><div class="kpi-value">${UI.money(totalRevenue)}</div></div>
        <div class="kpi red"><div class="kpi-label">Total Costs</div><div class="kpi-value">${UI.money(totalCosts)}</div></div>
        <div class="kpi ${netProfit>=0?'green':'red'}"><div class="kpi-label">Net Profit</div><div class="kpi-value">${UI.money(netProfit)}</div></div>
        <div class="kpi blue"><div class="kpi-label">Outstanding</div><div class="kpi-value">${UI.money(totalOutstanding)}</div></div>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
        <div class="card">
          <div class="card-title">Farm Overview</div>
          <div class="report-row"><span class="label">Farmers</span><span class="value">${farmers.length}</span></div>
          <div class="report-row"><span class="label">Fields / Lands</span><span class="value">${lands.length} (${UI.n(totalAc,1)} ac / ${UI.n(totalHa,2)} ha)</span></div>
          <div class="report-row"><span class="label">Harvest Records</span><span class="value">${harvests.length}</span></div>
          <div class="report-row"><span class="label">Total Yield</span><span class="value text-green fw-bold">${UI.n(totalYield,1)} tons</span></div>
          <div class="report-row"><span class="label">Total Bales</span><span class="value fw-bold">${totalBales}</span></div>
          <div class="report-row"><span class="label">Packaging Records</span><span class="value">${packaging.length}</span></div>
        </div>
        <div class="card">
          <div class="card-title">Financial Summary</div>
          <div class="report-row"><span class="label">Revenue</span><span class="value text-green fw-bold">${UI.money(totalRevenue)}</span></div>
          <div class="report-row negative"><span class="label">Total Costs</span><span class="value">${UI.money(totalCosts)}</span></div>
          <div class="report-row total"><span class="label">Net Profit</span><span class="value ${netProfit>=0?'text-green':'text-red'}">${UI.money(netProfit)}</span></div>
          <div class="report-row warning"><span class="label">Outstanding (owed)</span><span class="value">${UI.money(totalOutstanding)}</span></div>
          <div class="report-row"><span class="label">Transport Costs</span><span class="value">${UI.money(totalTransport)}</span></div>
          <div class="report-row"><span class="label">Inventory Value</span><span class="value">${UI.money(totalInvValue)}</span></div>
        </div>
        <div class="card">
          <div class="card-title">Cattle & Fattening</div>
          <div class="report-row"><span class="label">Total Cattle</span><span class="value">${cattle.length}</span></div>
          <div class="report-row"><span class="label">Active Cattle</span><span class="value text-green fw-bold">${activeCattle}</span></div>
          <div class="report-row"><span class="label">In Fattening</span><span class="value text-blue fw-bold">${activeFattening}</span></div>
          <div class="report-row negative"><span class="label">Feed Cost to Date</span><span class="value">${UI.money(totalFeedCost)}</span></div>
          <div class="report-row"><span class="label">Buyers</span><span class="value">${buyers.length}</span></div>
          <div class="report-row"><span class="label">Suppliers</span><span class="value">${sellers.length}</span></div>
          <div class="report-row"><span class="label">Total Sales</span><span class="value">${sales.length}</span></div>
        </div>
        <div class="card">
          <div class="card-title">Cost Breakdown by Category</div>
          ${costBreakdown || '<div style="color:#999;padding:8px 0">No costs recorded yet</div>'}
        </div>
      </div>
      <div class="card" style="margin-top:16px">
        <div class="card-title">Recent Sales</div>
        ${recentSalesHtml}
      </div>`);
  },
};
