/** Sales & Delivery Module */
const Sales = {
  SILAGE_TYPES: ['Maize','Grass','Sorghum','Mixed','Other'],
  DELIVERY_STATUSES: ['Pending','In Transit','Delivered','Cancelled'],
  PAYMENT_STATUSES: ['Unpaid','Partial','Paid'],

  calcTotal(body) {
    const tons      = parseFloat(body.querySelector('#qtyTons').value)||0;
    const bales     = parseInt(body.querySelector('#balesCount').value)||0;
    const pTon      = parseFloat(body.querySelector('#pricePerTon').value)||0;
    const pBale     = parseFloat(body.querySelector('#pricePerBale').value)||0;
    const transport = parseFloat(body.querySelector('#transportCost').value)||0;
    const loading   = parseFloat(body.querySelector('#loadingCost').value)||0;
    const unloading = parseFloat(body.querySelector('#unloadingCost').value)||0;
    const paid      = parseFloat(body.querySelector('#amountPaid').value)||0;
    const silageCost = (tons * pTon) + (bales * pBale);
    const total      = silageCost + transport + loading + unloading;
    const bal        = total - paid;
    const el = body.querySelector('#calc-display');
    if (el) el.innerHTML =
      `Silage: ${UI.money(silageCost)} + Transport: ${UI.money(transport)} + Loading: ${UI.money(loading)} + Unloading: ${UI.money(unloading)} = <strong>Total: ${UI.money(total)}</strong> | Balance: <strong style="color:${bal>0?'#C62828':'#2E7D32'}">${UI.money(bal)}</strong>`;
    return { silageCost, total, bal };
  },

  async render() {
    UI.title('Sales & Delivery');
    const rows = await db.getAll('sales');
    const totalRev      = rows.reduce((s,r)=>s+(r.totalAmount||0),0);
    const totalOut      = rows.reduce((s,r)=>s+(r.balance||0),0);
    const totalTrans    = rows.reduce((s,r)=>s+(r.transportCost||0),0);
    const totalHandling = rows.reduce((s,r)=>s+(r.loadingCost||0)+(r.unloadingCost||0),0);
    const pending       = rows.filter(r=>r.deliveryStatus==='Pending').length;

    const tableRows = rows.length ? rows.map(s => {
      const handling = (s.loadingCost||0) + (s.unloadingCost||0);
      return `
      <tr>
        <td><strong>${s.invoiceNumber||''}</strong></td>
        <td>${s.buyerName||''}</td>
        <td>${s.silagType||''}</td>
        <td>${UI.n(s.quantityTons,1)}t / ${s.balesCount||0} bales</td>
        <td>${s.departureLocation||''} → ${s.deliveryLocation||''}<br><small>${UI.n(s.transportDistanceKm,0)} km</small></td>
        <td class="fw-bold">${UI.money(s.totalAmount)}</td>
        <td>${UI.money(s.transportCost)}</td>
        <td>${handling > 0 ? `<span title="Loading: ${UI.money(s.loadingCost||0)} / Unloading: ${UI.money(s.unloadingCost||0)}">${UI.money(handling)}</span>` : '—'}</td>
        <td>${UI.money(s.amountPaid)}</td>
        <td class="${s.balance>0?'text-red':'text-green'} fw-bold">${UI.money(s.balance)}</td>
        <td>${UI.deliveryBadge(s.deliveryStatus)}</td>
        <td>${UI.payBadge(s.paymentStatus)}</td>
        <td>${s.saleDate||''}</td>
        <td class="actions">
          ${s.deliveryStatus!=='Delivered'?`<button class="btn btn-sm btn-blue" onclick="Sales.markDelivered(${s.id})">✓ Delivered</button>`:''}
          ${s.paymentStatus!=='Paid'?`<button class="btn btn-sm btn-primary" onclick="Sales.markPaid(${s.id})">✓ Paid</button>`:''}
          <button class="btn btn-sm btn-outline" onclick="Sales.edit(${s.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Sales.del(${s.id})">Delete</button>
        </td>
      </tr>`;
    }).join('') : `<tr><td colspan="14">${UI.empty()}</td></tr>`;

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Revenue</div><div class="kpi-value">${UI.money(totalRev)}</div></div>
        <div class="kpi red"><div class="kpi-label">Outstanding</div><div class="kpi-value">${UI.money(totalOut)}</div></div>
        <div class="kpi blue"><div class="kpi-label">Transport Costs</div><div class="kpi-value">${UI.money(totalTrans)}</div></div>
        ${totalHandling > 0 ? `<div class="kpi orange"><div class="kpi-label">Loading / Unloading</div><div class="kpi-value">${UI.money(totalHandling)}</div></div>` : ''}
        <div class="kpi"><div class="kpi-label">Pending Deliveries</div><div class="kpi-value">${pending}</div></div>
      </div>
      <div class="toolbar">
        <input type="text" placeholder="Search sales..." oninput="Sales.search(this.value)">
        <button class="btn btn-primary" onclick="Sales.add()">+ New Sale</button>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table id="tbl">
            <thead><tr><th>Invoice</th><th>Buyer</th><th>Type</th><th>Qty</th><th>Route</th><th>Total</th><th>Transport</th><th>Handling</th><th>Paid</th><th>Balance</th><th>Delivery</th><th>Payment</th><th>Date</th><th>Actions</th></tr></thead>
            <tbody>${tableRows}</tbody>
          </table>
        </div>
      </div>`);
  },

  async search(q) {
    const rows = await db.getAll('sales');
    const filtered = q ? rows.filter(s => (s.buyerName+s.invoiceNumber+s.deliveryLocation).toLowerCase().includes(q.toLowerCase())) : rows;
    document.querySelector('#tbl tbody').innerHTML = filtered.map(s => {
      const handling = (s.loadingCost||0)+(s.unloadingCost||0);
      return `<tr><td>${s.invoiceNumber}</td><td>${s.buyerName}</td><td>${s.silagType}</td><td>${UI.n(s.quantityTons,1)}t/${s.balesCount} bales</td><td>${s.departureLocation}→${s.deliveryLocation}</td><td>${UI.money(s.totalAmount)}</td><td>${UI.money(s.transportCost)}</td><td>${handling>0?UI.money(handling):'—'}</td><td>${UI.money(s.amountPaid)}</td><td>${UI.money(s.balance)}</td><td>${UI.deliveryBadge(s.deliveryStatus)}</td><td>${UI.payBadge(s.paymentStatus)}</td><td>${s.saleDate}</td><td class="actions">${s.deliveryStatus!=='Delivered'?`<button class="btn btn-sm btn-blue" onclick="Sales.markDelivered(${s.id})">✓ Delivered</button>`:''} ${s.paymentStatus!=='Paid'?`<button class="btn btn-sm btn-primary" onclick="Sales.markPaid(${s.id})">✓ Paid</button>`:''}<button class="btn btn-sm btn-outline" onclick="Sales.edit(${s.id})">Edit</button><button class="btn btn-sm btn-danger" onclick="Sales.del(${s.id})">Del</button></td></tr>`;
    }).join('')||`<tr><td colspan="14" style="text-align:center;color:#999">No results</td></tr>`;
  },

  async form(s = {}) {
    const buyers = await db.getAll('buyers');
    return `
      <div class="form-row">
        <div class="form-group"><label>Buyer *</label>
          <select id="buyerId"><option value="">— Select Buyer —</option>${UI.objOptions(buyers,'id','name',s.buyerId)}</select></div>
        <div class="form-group"><label>Silage Type</label><select id="silagType">${UI.options(this.SILAGE_TYPES,s.silagType)}</select></div>
      </div>
      <div class="form-section">Quantity & Pricing</div>
      <div class="form-row">
        <div class="form-group"><label>Quantity (Tons)</label><input id="qtyTons" type="number" step="0.1" value="${s.quantityTons||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
        <div class="form-group"><label>Number of Bales</label><input id="balesCount" type="number" value="${s.balesCount||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Price per Ton ($)</label><input id="pricePerTon" type="number" step="0.01" value="${s.pricePerTon||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
        <div class="form-group"><label>Price per Bale ($)</label><input id="pricePerBale" type="number" step="0.01" value="${s.pricePerBale||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
      </div>
      <div class="form-section">🚛 Transport / Delivery</div>
      <div class="form-row">
        <div class="form-group"><label>Departure Location</label><input id="departureLocation" value="${s.departureLocation||''}"></div>
        <div class="form-group"><label>Delivery Location</label><input id="deliveryLocation" value="${s.deliveryLocation||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Distance (km)</label><input id="transportDistanceKm" type="number" step="0.1" value="${s.transportDistanceKm||''}"></div>
        <div class="form-group"><label>Transport Cost ($)</label><input id="transportCost" type="number" step="0.01" value="${s.transportCost||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Loading Cost ($)</label><input id="loadingCost" type="number" step="0.01" placeholder="0.00" value="${s.loadingCost||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
        <div class="form-group"><label>Unloading Cost ($)</label><input id="unloadingCost" type="number" step="0.01" placeholder="0.00" value="${s.unloadingCost||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
      </div>
      <div class="form-section">Payment</div>
      <div class="calc-display" id="calc-display">Enter quantities and prices above to calculate total</div>
      <div class="form-row mt-8">
        <div class="form-group"><label>Amount Paid ($)</label><input id="amountPaid" type="number" step="0.01" value="${s.amountPaid||''}" oninput="Sales.calcTotal(document.querySelector('.modal-body'))"></div>
        <div class="form-group"><label>Payment Status</label><select id="paymentStatus">${UI.options(this.PAYMENT_STATUSES,s.paymentStatus)}</select></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Sale Date</label><input id="saleDate" type="date" value="${s.saleDate||UI.today()}"></div>
        <div class="form-group"><label>Expected Delivery Date</label><input id="deliveryDate" type="date" value="${s.deliveryDate||''}"></div>
      </div>
      <div class="form-group"><label>Delivery Status</label><select id="deliveryStatus">${UI.options(this.DELIVERY_STATUSES,s.deliveryStatus||'Pending')}</select></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${s.notes||''}</textarea></div>`;
  },

  async add() {
    const formHtml = await this.form();
    UI.openModal('New Sale / Delivery', formHtml, async (body) => {
      const buyerId = parseInt(UI.val(body,'buyerId'));
      if (!buyerId) { alert('Select a buyer'); return false; }
      const buyers = await db.getAll('buyers');
      const buyer = buyers.find(b => b.id === buyerId);
      const tons      = UI.num(body,'qtyTons');
      const bales     = parseInt(UI.val(body,'balesCount'))||0;
      const pTon      = UI.num(body,'pricePerTon');
      const pBale     = UI.num(body,'pricePerBale');
      const transport = UI.num(body,'transportCost');
      const loading   = UI.num(body,'loadingCost');
      const unloading = UI.num(body,'unloadingCost');
      const paid      = UI.num(body,'amountPaid');
      const silageCost = (tons * pTon) + (bales * pBale);
      const total      = silageCost + transport + loading + unloading;
      const balance    = total - paid;
      await db.add('sales', {
        buyerId, buyerName: buyer?.name||'',
        silagType: UI.val(body,'silagType'),
        quantityTons: tons, balesCount: bales,
        pricePerTon: pTon, pricePerBale: pBale,
        silageCost, transportCost: transport,
        loadingCost: loading, unloadingCost: unloading,
        transportDistanceKm: UI.num(body,'transportDistanceKm'),
        departureLocation: UI.val(body,'departureLocation'),
        deliveryLocation: UI.val(body,'deliveryLocation'),
        totalAmount: total, amountPaid: paid, balance,
        saleDate: UI.val(body,'saleDate'),
        deliveryDate: UI.val(body,'deliveryDate'),
        deliveryStatus: UI.val(body,'deliveryStatus')||'Pending',
        paymentStatus: UI.val(body,'paymentStatus')||'Unpaid',
        invoiceNumber: UI.invoiceNum(),
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async edit(id) {
    const s = await db.get('sales', id);
    const formHtml = await this.form(s);
    UI.openModal('Edit Sale', formHtml, async (body) => {
      const buyerId   = parseInt(UI.val(body,'buyerId'));
      const buyers    = await db.getAll('buyers');
      const buyer     = buyers.find(b => b.id === buyerId);
      const tons      = UI.num(body,'qtyTons');
      const bales     = parseInt(UI.val(body,'balesCount'))||0;
      const pTon      = UI.num(body,'pricePerTon');
      const pBale     = UI.num(body,'pricePerBale');
      const transport = UI.num(body,'transportCost');
      const loading   = UI.num(body,'loadingCost');
      const unloading = UI.num(body,'unloadingCost');
      const paid      = UI.num(body,'amountPaid');
      const silageCost = (tons * pTon) + (bales * pBale);
      const total      = silageCost + transport + loading + unloading;
      const balance    = total - paid;
      await db.put('sales', { ...s, buyerId, buyerName: buyer?.name||s.buyerName,
        silagType: UI.val(body,'silagType'),
        quantityTons: tons, balesCount: bales, pricePerTon: pTon, pricePerBale: pBale,
        silageCost, transportCost: transport,
        loadingCost: loading, unloadingCost: unloading,
        transportDistanceKm: UI.num(body,'transportDistanceKm'),
        departureLocation: UI.val(body,'departureLocation'),
        deliveryLocation: UI.val(body,'deliveryLocation'),
        totalAmount: total, amountPaid: paid, balance,
        saleDate: UI.val(body,'saleDate'), deliveryDate: UI.val(body,'deliveryDate'),
        deliveryStatus: UI.val(body,'deliveryStatus'),
        paymentStatus: UI.val(body,'paymentStatus'),
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async markDelivered(id) {
    const s = await db.get('sales', id);
    await db.put('sales', { ...s, deliveryStatus: 'Delivered' });
    this.render();
  },

  async markPaid(id) {
    const s = await db.get('sales', id);
    await db.put('sales', { ...s, amountPaid: s.totalAmount, balance: 0, paymentStatus: 'Paid' });
    this.render();
  },

  async del(id) {
    if (!UI.confirm('Delete this sale record?')) return;
    await db.delete('sales', id);
    this.render();
  },
};
