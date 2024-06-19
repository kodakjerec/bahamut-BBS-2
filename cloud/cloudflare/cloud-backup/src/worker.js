
export default {
  async fetch(request, env, ctx) {
    const { DATABASE } = env
    const fromData = (await request.formData())
    try {
      let userId = ''
      let jsonData = ''
      try {
        userId = fromData.get('userId')
        jsonData = fromData.get('jsonData')
      } catch {
        return Response.json({ 'error': 'No userId or data'})
      }
      const eventDate = new Date()
      eventDate.setUTCHours(eventDate.getUTCHours()+8)
      const lastTime = eventDate.toLocaleString('zh-TW', { hour12: false })

      const stmt = DATABASE.prepare("SELECT userId FROM user_set where userId=? LIMIT 1").bind(userId);
      const { results } = await stmt.all();
      
      if (results && results.length>0) {
        // update
        const stmtUpdate = DATABASE.prepare("UPDATE user_set SET lastTime=?, jsonData=?  WHERE userId=?").bind(lastTime, jsonData, userId)
        await stmtUpdate.all()
      } else {
        // new
        const stmtInsert = DATABASE.prepare("INSERT INTO user_set VALUES(?, ?, ?)").bind(userId, lastTime, jsonData)
        await stmtInsert.all()
      }
      return Response.json({ 'error': '', 'lastTime': lastTime })
    } catch (exception) {
      console.log(exception)
      return Response.json({ 'error': 'Error' })
    }
  },
};