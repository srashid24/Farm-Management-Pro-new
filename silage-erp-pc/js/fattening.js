const Fattening = {
  FEED_TYPES: ['Silage Only','Grain + Silage','TMR','Pasture + Silage','Other'],
  async render() {
    UI.title('Fattening Program');
    const rows = await db.getAll('fattening');
    const active = rows.filter(r=>r.status==='Active');
    const totalFeedCost = active.reduce((s,r)=>s+(r.totalFeedCostToDate||0),0);
    const tableRows = rows.length ? rows.map(f=>{
      const gain = (f.currentWeightKg||0)-(f.startWeightKg||0);
      const needed = (f.targetWeightKg||0)-(f.startWeightKg||0);
      const pct = needed>0 ? Math.min(100,Math.round((gain/needed)*100)) : 0;
      return `<tr>
        <td><strong>${f.cattleTag||''}</strong> ${f.cattleName?'('+f.cattleName+')':''}</td>
        <td>${f.startDate||''}</td>
        <td>${UI.n(f.startWeightKg,1)}</td>
        <td class="fw-bold text-green">${UI.n(f.currentWeightKg,1)}</td>
        <td>${UI.n(f.targetWeightKg,1)}</td>
        <td>+${UI.n(gain,1)} kg</td>
        <td><div class="progress-wrap" style="min-width:80px"><div class="progress-bar" style="width:${pct}%"></div></div><small>${pct}%</small></td>
        <td>${UI.n(f.dailyFeedKg,1)} kg/day</td><td>${f.feedType||''}</td>
        <td class="text-red fw-bold">${UI.money(f.totalFeedCostToDate)}</td>
        <td>${UI.badge(f.status, f.status==='Active'?'green':f.status==='Completed'?'blue':'grey')}</td>
        <td class="actions">
          <button class="btn btn-sm btn-blue" onclick="Fattening.updateWeight(${f.id})">Update</button>
          <button class="btn btn-sm btn-outline" onclick="Fattening.edit(${f.id})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="Fattening.del(${f.id})">Del</button>
        </td></tr>`;
    }).join('') : `<tr><td colspan="12">${UI.empty('No fattening programs yet. Add cattle first, then start a fattening program.')}</td></tr>`;
    UI.render(`
      <div class="kpi-grid">
        <div class="kpi blue"><div class="kpi-label">Active Programs</div><div class="kpi-value">${active.length}</div></div>
        <div class="kpi red"><div class="kpi-label">Total Feed Cost</div><div class="kpi-value">${UI.money(totalFeedCost)}</div></div>
      </div>
      <div class="toolbar"><button class="btn btn-primary" onclick="Fattening.add()">+ Start Fattening Program</button></div>
      <div class="card"><div class="table-wrap"><table>
        <thead><tr><th>Cattle</th><th>Start Date</th><th>Start Wt</th><th>Current Wt</th><th>Target Wt</th><th>Gain</th><th>Progress</th><th>Daily Feed</th><th>Feed Type</th><th>Feed Cost</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${tableRows}</tbody></table></div></div>`);
  },
  async form(f={}) {
    const cattle = await db.getAll('cattle');
    return `<div class="form-group"><label>Cattle *</label><select id="cattleId"><option value="">— Select Cattle —</option>${cattle.map(c=>`<option value="${c.id}" ${c.id==f.cattleId?'selected':''}>${c.tagNumber} ${c.name?'('+c.name+')':''}</option>`).join('')}</select></div>
      <div class="form-row"><div class="form-group"><label>Start Date</label><input id="startDate" type="date" value="${f.startDate||UI.today()}"></div>
        <div class="form-group"><label>Feed Type</label><select id="feedType">${UI.options(this.FEED_TYPES,f.feedType)}</select></div></div>
      <div class="form-row-3">
        <div class="form-group"><label>Start Weight (kg)</label><input id="startWeightKg" type="number" step="0.1" value="${f.startWeightKg||''}"></div>
        <div class="form-group"><label>Target Weight (kg)</label><input id="targetWeightKg" type="number" step="0.1" value="${f.targetWeightKg||''}"></div>
        <div class="form-group"><label>Daily Feed (kg/day)</label><input id="dailyFeedKg" type="number" step="0.1" value="${f.dailyFeedKg||''}"></div>
      </div>
      <div class="form-group"><label>Notes</label><textarea id="notes">${f.notes||''}</textarea></div>`;
  },
  async add() { const fh=await this.form(); UI.openModal('Start Fattening Program',fh,async(body)=>{ const cid=parseInt(UI.val(body,'cattleId')); if(!cid){alert('Select cattle');return false;} const cattle=await db.getAll('cattle'); const c=cattle.find(x=>x.id===cid); const sw=UI.num(body,'startWeightKg'); await db.add('fattening',{cattleId:cid,cattleTag:c?.tagNumber||'',cattleName:c?.name||'',startDate:UI.val(body,'startDate'),startWeightKg:sw,currentWeightKg:sw,targetWeightKg:UI.num(body,'targetWeightKg'),dailyFeedKg:UI.num(body,'dailyFeedKg'),feedType:UI.val(body,'feedType'),totalFeedCostToDate:0,status:'Active',notes:UI.val(body,'notes')}); this.render(); }); },
  async edit(id) { const f=await db.get('fattening',id); const fh=await this.form(f); UI.openModal('Edit Fattening',fh,async(body)=>{ const sw=UI.num(body,'startWeightKg'); await db.put('fattening',{...f,startDate:UI.val(body,'startDate'),startWeightKg:sw,targetWeightKg:UI.num(body,'targetWeightKg'),dailyFeedKg:UI.num(body,'dailyFeedKg'),feedType:UI.val(body,'feedType'),notes:UI.val(body,'notes')}); this.render(); }); },
  async updateWeight(id) {
    const f = await db.get('fattening', id);
    UI.openModal('Update Weight & Feed Cost', `
      <div class="form-group"><label>Current Weight (kg)</label><input id="currentWeightKg" type="number" step="0.1" value="${f.currentWeightKg||''}"></div>
      <div class="form-group"><label>Feed Cost to Add ($)</label><input id="feedCostAdd" type="number" step="0.01" value=""></div>
      <div class="form-group"><label>Status</label><select id="status"><option ${f.status==='Active'?'selected':''}>Active</option><option ${f.status==='Completed'?'selected':''}>Completed</option><option ${f.status==='Sold'?'selected':''}>Sold</option></select></div>`,
      async (body) => {
        const newWt = UI.num(body,'currentWeightKg');
        const addCost = UI.num(body,'feedCostAdd');
        await db.put('fattening', { ...f, currentWeightKg: newWt, totalFeedCostToDate: (f.totalFeedCostToDate||0)+addCost, status: UI.val(body,'status') });
        this.render();
      });
  },
  async del(id) { if(!UI.confirm('Delete this fattening record?'))return; await db.delete('fattening',id); this.render(); },
};
