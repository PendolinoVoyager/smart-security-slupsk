use loco_rs::schema::table_auto_tz;
use sea_orm_migration::{prelude::*, schema::*};

#[derive(DeriveMigrationName)]
pub struct Migration;

#[async_trait::async_trait]
impl MigrationTrait for Migration {
    async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .create_table(
                table_auto_tz(Devices::Table)
                    .col(pk_auto(Devices::Id))
                    .col(integer(Devices::OwnerId))
                    .col(integer(Devices::SelfUserId))
                    .foreign_key(
                        ForeignKey::create()
                            .name("fk-devices-owners")
                            .from(Devices::Table, Devices::OwnerId)
                            .to(Devices::Table, Devices::Id)
                            .on_delete(ForeignKeyAction::Cascade)
                            .on_update(ForeignKeyAction::Cascade),
                    )
                    .foreign_key(
                        ForeignKey::create()
                            .name("fk-devices-self_users")
                            .from(Devices::Table, Devices::SelfUserId)
                            .to(Devices::Table, Devices::Id)
                            .on_delete(ForeignKeyAction::Cascade)
                            .on_update(ForeignKeyAction::Cascade),
                    )
                    .to_owned(),
            )
            .await
    }

    async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .drop_table(Table::drop().table(Devices::Table).to_owned())
            .await
    }
}

#[derive(DeriveIden)]
enum Devices {
    Table,
    Id,
    OwnerId,
    SelfUserId,
}
#[allow(unused)]
#[derive(DeriveIden)]
enum Users {
    Table,
    Id,
}
