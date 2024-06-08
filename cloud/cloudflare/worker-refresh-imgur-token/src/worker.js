export default {
  async fetch(request, env, ctx) {
    const { DATABASE } = env;
    const stmt = DATABASE.prepare("SELECT accessToken, albumHash FROM imgur_token");
    const { results } = await stmt.all();

    const data = results[0]
    console.log(data)
    clientId = data.get('clientId')
    clientSecret = data.get('clientSecret')
    accessToken = data.get('accessToken')
    refreshToken = data.get('refreshToken')
    return Response.json(results[0]);
  },
};