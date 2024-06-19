export default {
  async fetch(request, env) {
    let fromData = ''
      let userId = ''
      let buyType = ''
      let qty = 0
      let purchaseData = ''
      try {
        fromData = await request.formData()
        userId = fromData.get('userId')
        buyType = fromData.get('buyType')
      } catch {
        return Response.json({'error': 'No userId '})
      }
      try {
        const { DATABASE } = env;
        if (buyType == 'history') {
          // Update or new
          qty = fromData.get('qty')
          purchaseData = fromData.get('purchaseData')

          const stmt = DATABASE.prepare('SELECT Id FROM user_buy WHERE userId=? AND buyType=?').bind(userId, buyType);
          const { results } = await stmt.all();
          if (results && results.length>0) {
            // Update
            const stmt2 = DATABASE.prepare('UPDATE user_buy SET qty=?, purchaseData=? WHERE userId=? AND buyType=?').bind(qty, purchaseData, userId, buyType);
            await stmt2.all();
            return Response.json({'error': ''})
          } else {
            // new
            const stmt2 = DATABASE.prepare('SELECT MAX(Id) as Id FROM user_buy');
            const { results } = await stmt2.all();
            let nextId = 0;
            if (results && results.length>0) {
              nextId = results[0]['Id']
              if (nextId!=null)
                nextId = nextId + 1;
            }

            const stmt3 = DATABASE.prepare('INSERT INTO user_buy VALUES(?,?,?,?,?)').bind(nextId, userId, buyType, qty, purchaseData);
            await stmt3.all();
            return Response.json({'error': ''})
          }
        } else if (buyType == 'purchase') {
          // new
          qty = fromData.get('qty')
          purchaseData = fromData.get('purchaseData')

          const stmt = DATABASE.prepare('SELECT MAX(Id) as Id FROM user_buy');
          const { results } = await stmt.all();
          let nextId = 0;
          if (results && results.length>0) {
            nextId = results[0]['Id']
            if (nextId!=null)
              nextId = nextId + 1;
          }

          const stmt2 = DATABASE.prepare('INSERT INTO user_buy VALUES(?,?,?,?,?)').bind(nextId, userId, buyType, qty, purchaseData);
          await stmt2.all();
          return Response.json({'error': ''})
        } else if (buyType=='query') {
          let buyQty = 0;
          const stmt = DATABASE.prepare('SELECT SUM(qty) as qty FROM user_buy WHERE userId=?').bind(userId);
          const { results } = await stmt.all();
          if (results && results.length>0) {
            const queryQty = results[0]['qty']
            if (queryQty!=null)
              buyQty = queryQty;
          }
          return Response.json({'error': '', 'qty': buyQty})
        }
        return Response.json({'error': ''})
      } catch (e) {
        console.log(e)
        return Response.json({'error': ' Something got error '})
      }
    }
  }
  