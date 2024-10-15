use sea_orm_migration::prelude::*;
#[derive(DeriveMigrationName)]
pub struct Migration;

#[derive(Debug, DeriveIden)]
#[sea_orm(enum_name = "role")]
#[sea_orm(rs_type = "i32", db_type = "Integer")]
pub enum UserRole {
    #[sea_orm(iden = "role")]
    Enum,
    Device = 1,
    User = 2,
    Admin = 3,
}
#[derive(DeriveIden)]
enum Users {
    Table,
    Role,
}

#[async_trait::async_trait]
impl MigrationTrait for Migration {
    async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        // first add enum type
        manager
            .create_type(
                extension::postgres::Type::create()
                    .as_enum(UserRole::Enum)
                    .values([UserRole::Device, UserRole::User, UserRole::Admin])
                    .to_owned(),
            )
            .await?;
        // add column
        manager
            .alter_table(
                Table::alter()
                    .table(Users::Table)
                    .add_column_if_not_exists(
                        ColumnDef::new(Users::Role)
                            .custom(UserRole::Enum)
                            .not_null(),
                    )
                    .to_owned(),
            )
            .await

        //
        // delete column
        //
        /*
        manager
            .alter_table(
                Table::alter()
                    .table(Movies::Table)
                    .drop_column(Movies::Rating)
                    .to_owned(),
            )
            .await
        */
    }

    async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .drop_type(
                extension::postgres::Type::drop()
                    .name(UserRole::Enum)
                    .if_exists()
                    .to_owned(),
            )
            .await?;
        manager
            .alter_table(
                Table::alter()
                    .table(Users::Table)
                    .drop_column(Users::Role)
                    .to_owned(),
            )
            .await?;
        Ok(())
    }
}
