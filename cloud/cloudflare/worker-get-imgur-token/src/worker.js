export default {
async fetch(request, env) {
    const { DATABASE } = env;
    const stmt = DATABASE.prepare("SELECT accessToken, albumHash FROM imgur_token");
    const { results } = await stmt.all();
    return Response.json(results[0]);
  }
}
