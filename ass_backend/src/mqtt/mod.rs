use clap::Parser;

#[derive(Parser)]
struct Cli {
    test: String,
}

pub fn main() {
    let args = Cli::parse();
    println!("Hello, world! You provided: {} as DB URI!", args.test)
}
