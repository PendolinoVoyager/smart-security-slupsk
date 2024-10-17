use crate::{models::_entities::users, views::user::CurrentResponse};
use axum::debug_handler;
use loco_rs::controller::middleware::auth::ApiToken;
use loco_rs::prelude::*;
#[debug_handler]
async fn current(auth: auth::JWT, State(ctx): State<AppContext>) -> Result<Response> {
    let user = users::Model::find_by_pid(&ctx.db, &auth.claims.pid).await?;
    format::json(CurrentResponse::new(&user))
}

#[debug_handler]
async fn with_api_key(
    auth: ApiToken<users::Model>,
    State(_ctx): State<AppContext>,
) -> Result<Response> {
    format::text(&format!("Key is good: {}", &auth.user.email))
}
pub fn routes() -> Routes {
    Routes::new()
        .prefix("user")
        .add("/current", get(current))
        .add("/current_key", get(with_api_key))
}
