const Packaging = {
  PKG_TYPES: ['Round Bale','Square Bale','Silage Pit','Silage Bag','Other'],
  WRAP_LAYERS: ['4 Layers','6 Layers','8 Layers'],

  async render() {
    UI.title('Packaging');
    const rows = await db.getAll('packaging');
    const totalBales    = rows.reduce((s,r)=>s+(r.baleCount||0),0);
    const totalTons     = rows.reduce((s,r)=>s+(r.totalWeightTons||0),0);
    const totalCost     = rows.reduce((s,r)=>s+(r.totalPackagingCost||0),0);
    const totalHandling = rows.reduce((s,r)=>s+(r.loadingCost||0)+(r.unloadingCost||0),0);
    const totalExtras   = rows.reduce((s,r)=>s+(r.additionalExpensesTotal||0),0);

    const tableRows = rows.length ? rows.map(p => {
      const handlingCost = (p.loadingCost||0)+(p.unloadingCost||0);
      const batchCount   = p.batchShifts?.length || 0;
      const totalBatchBags = (p.batchShifts||[]).reduce((s,b)=>s+(b.bagsProduced||0),0);
      return `<tr>
        <td>${p.packagingDate||''}</td>
        <td>${p.harvestSeason||''}</td>
        <td>${p.landName||''}</td>
        <td class="fw-bold text-green">${p.baleCount||0} bales</td>
        <td>${UI.n(p.baleWeightKg,1)} kg</td>
        <td>${UI.n(p.totalWeightTons,2)} t</td>
        <td>${p.packagingType||''}</td>
        <td>${p.wrapLayers||''}</td>
        <td>${p.wrapColour||''}</td>
        <td>${p.shiftsWorked||'—'}</td>
        <td>${p.bagsPerShift ? `${p.bagsPerShift}/shift` : '—'}</td>
        <td>${batchCount > 0 ? `<span title="${(p.batchShifts||[]).map(b=>`Batch ${b.batchNumber} (${b.shift}): ${b.bagsProduced} bags`).join('\n')}">${batchCount} batches · ${totalBatchBags} bags</span>` : '—'}</td>
        <td class="text-red">${UI.money(p.totalPackagingCost||0)}</td>
        <td>${handlingCost > 0 ? UI.money(handlingCost) : '—'}</td>
        <td>${p.additionalExpensesTotal > 0 ? `<span title="${(p.additionalExpenses||[]).map(e=>e.type+': '+UI.money(e.amount)).join(', ')}">${UI.money(p.additionalExpensesTotal)}</span>` : '—'}</td>
        <td>${p.storageLocation||''}</td>
        <td class="actions">
          <button class="btn btn-sm btn-outline" onclick="Packaging.edit(${p.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Packaging.del(${p.id})">Delete</button>
        </td>
      </tr>`;
    }).join('') : `<tr><td colspan="17">${UI.empty()}</td></tr>`;

    UI.render(`
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Bales</div><div class="kpi-value">${totalBales}</div></div>
        <div class="kpi brown"><div class="kpi-label">Total Weight</div><div class="kpi-value">${UI.n(totalTons,1)} t</div></div>
        <div class="kpi red"><div class="kpi-label">Packaging Cost</div><div class="kpi-value">${UI.money(totalCost)}</div></div>
        ${totalHandling > 0 ? `<div class="kpi orange"><div class="kpi-label">Loading / Unloading</div><div class="kpi-value">${UI.money(totalHandling)}</div></div>` : ''}
        ${totalExtras > 0 ? `<div class="kpi"><div class="kpi-label">Extra Expenses</div><div class="kpi-value">${UI.money(totalExtras)}</div></div>` : ''}
      </div>
      <div class="toolbar"><button class="btn btn-primary" onclick="Packaging.add()">+ Add Packaging Record</button></div>
      <div class="card"><div class="table-wrap"><table>
        <thead><tr><th>Date</th><th>Season</th><th>Field</th><th>Bales</th><th>Bale Wt</th><th>Total Wt</th><th>Type</th><th>Layers</th><th>Colour</th><th>Shifts</th><th>Bags/Shift</th><th>Batch Detail</th><th>Pkg Cost</th><th>Handling</th><th>Extras</th><th>Storage</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody>
      </table></div></div>`);
  },

  async form(p = {}) {
    const harvests = await db.getAll('harvests');
    return `
      <div class="form-group"><label>Harvest *</label>
        <select id="harvestId"><option value="">— Select Harvest —</option>
          ${harvests.map(h=>`<option value="${h.id}" ${h.id==p.harvestId?'selected':''}>${h.season} | ${h.silagType}</option>`).join('')}
        </select>
      </div>
      <div class="form-group"><label>Packaging Date</label><input id="packagingDate" type="date" value="${p.packagingDate||UI.today()}"></div>
      <div class="form-row">
        <div class="form-group"><label>Number of Bales / Bags</label><input id="baleCount" type="number" value="${p.baleCount||''}"></div>
        <div class="form-group"><label>Bale / Bag Weight (kg)</label><input id="baleWeightKg" type="number" step="0.1" value="${p.baleWeightKg||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Packaging Type</label><select id="packagingType">${UI.options(this.PKG_TYPES,p.packagingType)}</select></div>
        <div class="form-group"><label>Wrap Layers</label><select id="wrapLayers">${UI.options(this.WRAP_LAYERS,p.wrapLayers)}</select></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Wrap Colour</label><input id="wrapColour" value="${p.wrapColour||''}"></div>
        <div class="form-group"><label>Wrap Cost per Bale ($)</label><input id="wrapCostPerBale" type="number" step="0.01" value="${p.wrapCostPerBale||''}"></div>
      </div>
      <div class="form-section">👷 Labour / Shift Overview</div>
      <div class="form-row">
        <div class="form-group"><label>Number of Shifts Worked</label><input id="shiftsWorked" type="number" step="1" placeholder="e.g. 3" value="${p.shiftsWorked||''}"></div>
        <div class="form-group"><label>Bags / Bales per Shift (avg)</label><input id="bagsPerShift" type="number" step="1" placeholder="e.g. 120" value="${p.bagsPerShift||''}"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Contractor</label><input id="contractor" value="${p.contractor||''}"></div>
        <div class="form-group"><label>Contractor Cost ($)</label><input id="contractorCost" type="number" step="0.01" value="${p.contractorCost||''}"></div>
      </div>
      ${UI.batchShiftField(p.batchShifts||[])}
      <div class="form-section">🏗️ Loading / Unloading</div>
      <div class="form-row">
        <div class="form-group"><label>Loading Cost ($)</label><input id="loadingCost" type="number" step="0.01" placeholder="0.00" value="${p.loadingCost||''}"></div>
        <div class="form-group"><label>Unloading Cost ($)</label><input id="unloadingCost" type="number" step="0.01" placeholder="0.00" value="${p.unloadingCost||''}"></div>
      </div>
      ${UI.additionalExpensesField(p.additionalExpenses||[])}
      <div class="form-group" style="margin-top:8px"><label>Storage Location</label><input id="storageLocation" value="${p.storageLocation||''}"></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${p.notes||''}</textarea></div>`;
  },

  async add() {
    const fh = await this.form();
    UI.openModal('Add Packaging Record', fh, async (body) => {
      const hid = parseInt(UI.val(body,'harvestId'));
      if (!hid) { alert('Select harvest'); return false; }
      const harvests = await db.getAll('harvests');
      const h = harvests.find(x => x.id === hid);
      const bales      = parseInt(UI.val(body,'baleCount'))||0;
      const bwt        = UI.num(body,'baleWeightKg');
      const wc         = UI.num(body,'wrapCostPerBale');
      const contractor = UI.num(body,'contractorCost');
      const loading    = UI.num(body,'loadingCost');
      const unloading  = UI.num(body,'unloadingCost');
      const { list: additionalExpenses, total: additionalExpensesTotal } = UI.readAdditionalExpenses(body);
      const { list: batchShifts, totalBags: batchTotalBags } = UI.readBatchShifts(body);
      const totalWrapCost      = bales * wc;
      const totalPackagingCost = totalWrapCost + contractor + loading + unloading + additionalExpensesTotal;
      await db.add('packaging', {
        harvestId: hid, harvestSeason: h?.season||'', landName: h?.silagType||'',
        baleCount: bales, baleWeightKg: bwt, totalWeightTons: (bales*bwt)/1000,
        packagingType: UI.val(body,'packagingType'), wrapLayers: UI.val(body,'wrapLayers'),
        wrapColour: UI.val(body,'wrapColour'), wrapCostPerBale: wc, totalWrapCost,
        shiftsWorked: parseInt(UI.val(body,'shiftsWorked'))||0,
        bagsPerShift: parseInt(UI.val(body,'bagsPerShift'))||0,
        batchShifts, batchTotalBags,
        packagingDate: UI.val(body,'packagingDate'),
        storageLocation: UI.val(body,'storageLocation'),
        contractor: UI.val(body,'contractor'), contractorCost: contractor,
        loadingCost: loading, unloadingCost: unloading,
        additionalExpenses, additionalExpensesTotal,
        totalPackagingCost,
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async edit(id) {
    const p = await db.get('packaging', id);
    const fh = await this.form(p);
    UI.openModal('Edit Packaging', fh, async (body) => {
      const bales      = parseInt(UI.val(body,'baleCount'))||0;
      const bwt        = UI.num(body,'baleWeightKg');
      const wc         = UI.num(body,'wrapCostPerBale');
      const contractor = UI.num(body,'contractorCost');
      const loading    = UI.num(body,'loadingCost');
      const unloading  = UI.num(body,'unloadingCost');
      const { list: additionalExpenses, total: additionalExpensesTotal } = UI.readAdditionalExpenses(body);
      const { list: batchShifts, totalBags: batchTotalBags } = UI.readBatchShifts(body);
      const totalWrapCost      = bales * wc;
      const totalPackagingCost = totalWrapCost + contractor + loading + unloading + additionalExpensesTotal;
      await db.put('packaging', {
        ...p,
        baleCount: bales, baleWeightKg: bwt, totalWeightTons: (bales*bwt)/1000,
        packagingType: UI.val(body,'packagingType'), wrapLayers: UI.val(body,'wrapLayers'),
        wrapColour: UI.val(body,'wrapColour'), wrapCostPerBale: wc, totalWrapCost,
        shiftsWorked: parseInt(UI.val(body,'shiftsWorked'))||0,
        bagsPerShift: parseInt(UI.val(body,'bagsPerShift'))||0,
        batchShifts, batchTotalBags,
        packagingDate: UI.val(body,'packagingDate'),
        storageLocation: UI.val(body,'storageLocation'),
        contractor: UI.val(body,'contractor'), contractorCost: contractor,
        loadingCost: loading, unloadingCost: unloading,
        additionalExpenses, additionalExpensesTotal,
        totalPackagingCost,
        notes: UI.val(body,'notes'),
      });
      this.render();
    });
  },

  async del(id) {
    if (!UI.confirm('Delete this packaging record?')) return;
    await db.delete('packaging', id);
    this.render();
  },
};
