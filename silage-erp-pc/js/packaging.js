const Packaging = {
  PKG_TYPES: ['Round Bale','Square Bale','Silage Pit','Silage Bag','Other'],
  WRAP_LAYERS: ['4 Layers','6 Layers','8 Layers'],
  async render() {
    UI.title('Packaging');
    const rows = await db.getAll('packaging');
    const totalBales = rows.reduce((s,r)=>s+(r.baleCount||0),0);
    const totalTons = rows.reduce((s,r)=>s+(r.totalWeightTons||0),0);
    const totalCost = rows.reduce((s,r)=>s+((r.totalWrapCost||0)+(r.contractorCost||0)),0);
    const tableRows = rows.length ? rows.map(p=>`<tr>
      <td>${p.packagingDate||''}</td><td>${p.harvestSeason||''}</td><td>${p.landName||''}</td>
      <td class="fw-bold text-green">${p.baleCount||0} bales</td><td>${UI.n(p.baleWeightKg,1)} kg</td>
      <td>${UI.n(p.totalWeightTons,2)} t</td><td>${p.packagingType||''}</td>
      <td>${p.wrapLayers||''}</td><td>${p.wrapColour||''}</td>
      <td class="text-red">${UI.money((p.totalWrapCost||0)+(p.contractorCost||0))}</td>
      <td>${p.storageLocation||''}</td>
      <td class="actions"><button class="btn btn-sm btn-outline" onclick="Packaging.edit(${p.id})">Edit</button>
      <button class="btn btn-sm btn-danger" onclick="Packaging.del(${p.id})">Delete</button></td></tr>`).join('') : `<tr><td colspan="12">${UI.empty()}</td></tr>`;
    UI.render(`
      <div class="kpi-grid">
        <div class="kpi green"><div class="kpi-label">Total Bales</div><div class="kpi-value">${totalBales}</div></div>
        <div class="kpi brown"><div class="kpi-label">Total Weight</div><div class="kpi-value">${UI.n(totalTons,1)} t</div></div>
        <div class="kpi red"><div class="kpi-label">Packaging Cost</div><div class="kpi-value">${UI.money(totalCost)}</div></div>
      </div>
      <div class="toolbar"><button class="btn btn-primary" onclick="Packaging.add()">+ Add Packaging Record</button></div>
      <div class="card"><div class="table-wrap"><table>
        <thead><tr><th>Date</th><th>Season</th><th>Field</th><th>Bales</th><th>Bale Wt</th><th>Total</th><th>Type</th><th>Layers</th><th>Colour</th><th>Cost</th><th>Storage</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },
  async form(p={}) {
    const harvests = await db.getAll('harvests');
    return `<div class="form-group"><label>Harvest *</label><select id="harvestId"><option value="">— Select Harvest —</option>${harvests.map(h=>`<option value="${h.id}" ${h.id==p.harvestId?'selected':''}>${h.season} | ${h.silagType}</option>`).join('')}</select></div>
      <div class="form-group"><label>Packaging Date</label><input id="packagingDate" type="date" value="${p.packagingDate||UI.today()}"></div>
      <div class="form-row"><div class="form-group"><label>Number of Bales</label><input id="baleCount" type="number" value="${p.baleCount||''}"></div>
        <div class="form-group"><label>Bale Weight (kg)</label><input id="baleWeightKg" type="number" step="0.1" value="${p.baleWeightKg||''}"></div></div>
      <div class="form-row"><div class="form-group"><label>Packaging Type</label><select id="packagingType">${UI.options(this.PKG_TYPES,p.packagingType)}</select></div>
        <div class="form-group"><label>Wrap Layers</label><select id="wrapLayers">${UI.options(this.WRAP_LAYERS,p.wrapLayers)}</select></div></div>
      <div class="form-row"><div class="form-group"><label>Wrap Colour</label><input id="wrapColour" value="${p.wrapColour||''}"></div>
        <div class="form-group"><label>Wrap Cost per Bale ($)</label><input id="wrapCostPerBale" type="number" step="0.01" value="${p.wrapCostPerBale||''}"></div></div>
      <div class="form-row"><div class="form-group"><label>Storage Location</label><input id="storageLocation" value="${p.storageLocation||''}"></div>
        <div class="form-group"><label>Contractor</label><input id="contractor" value="${p.contractor||''}"></div></div>
      <div class="form-group"><label>Contractor Cost ($)</label><input id="contractorCost" type="number" step="0.01" value="${p.contractorCost||''}"></div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${p.notes||''}</textarea></div>`;
  },
  async add() { const fh=await this.form(); UI.openModal('Add Packaging Record',fh,async(body)=>{ const hid=parseInt(UI.val(body,'harvestId')); if(!hid){alert('Select harvest');return false;} const harvests=await db.getAll('harvests'); const h=harvests.find(x=>x.id===hid); const bales=parseInt(UI.val(body,'baleCount'))||0; const bwt=UI.num(body,'baleWeightKg'); const wc=UI.num(body,'wrapCostPerBale'); await db.add('packaging',{harvestId:hid,harvestSeason:h?.season||'',landName:h?.silagType||'',baleCount:bales,baleWeightKg:bwt,totalWeightTons:(bales*bwt)/1000,packagingType:UI.val(body,'packagingType'),wrapLayers:UI.val(body,'wrapLayers'),wrapColour:UI.val(body,'wrapColour'),wrapCostPerBale:wc,totalWrapCost:bales*wc,packagingDate:UI.val(body,'packagingDate'),storageLocation:UI.val(body,'storageLocation'),contractor:UI.val(body,'contractor'),contractorCost:UI.num(body,'contractorCost'),notes:UI.val(body,'notes')}); this.render(); }); },
  async edit(id) { const p=await db.get('packaging',id); const fh=await this.form(p); UI.openModal('Edit Packaging',fh,async(body)=>{ const bales=parseInt(UI.val(body,'baleCount'))||0; const bwt=UI.num(body,'baleWeightKg'); const wc=UI.num(body,'wrapCostPerBale'); await db.put('packaging',{...p,baleCount:bales,baleWeightKg:bwt,totalWeightTons:(bales*bwt)/1000,packagingType:UI.val(body,'packagingType'),wrapLayers:UI.val(body,'wrapLayers'),wrapColour:UI.val(body,'wrapColour'),wrapCostPerBale:wc,totalWrapCost:bales*wc,packagingDate:UI.val(body,'packagingDate'),storageLocation:UI.val(body,'storageLocation'),contractor:UI.val(body,'contractor'),contractorCost:UI.num(body,'contractorCost'),notes:UI.val(body,'notes')}); this.render(); }); },
  async del(id) { if(!UI.confirm('Delete this packaging record?'))return; await db.delete('packaging',id); this.render(); },
};
